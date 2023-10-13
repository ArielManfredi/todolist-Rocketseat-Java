package br.com.arielxmanfredi.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.arielxmanfredi.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    

    @Autowired
    private ITaskRepository taskRepository;


    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request){

        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())){
            System.out.println("Data de inicio da tarefa é no passado!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/término deve ser maior do que a data atual!");
        }
        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            System.out.println("Data de inicio da tarefa é no passado!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor do que a data de término!");
        }

        var task = taskRepository.save(taskModel);
        return ResponseEntity.ok().body(task);
    }

    @GetMapping("/")
    public ResponseEntity<?> list(HttpServletRequest request){
        var idUser = request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);

        return ResponseEntity.status(HttpStatus.FOUND).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){
       
        var task = this.taskRepository.findById(id).orElse(null);
        if(task == null){
            return ResponseEntity.badRequest().body("Confira o ID da Tarefa.");
        }
        
        var idUser = request.getAttribute("idUser");
        if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autorizado à editar esta tarefa.");
        }
        Utils.copyNonNullProperties(taskModel, task);
        var taskSaved = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(taskSaved);
    }
}
