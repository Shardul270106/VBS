package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController
{
    @Autowired
    UserRepo userRepo;

    @Autowired
    HistoryRepo historyRepo;

    @Autowired
    TransactionRepo transactionRepo;
    @PostMapping("/register")
    public String register(@RequestBody User user )
    {
        userRepo.save(user);
        return "Signup Successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user=userRepo.findByUsername(u.getUsername());
        if(user==null)
        {
            return "User not found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Password is incorrect";
        }
        if(!u.getRole().equals(u.getRole()))
        {
            return "Role oncorrect";
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user=userRepo.findById(id).orElseThrow(()->new RuntimeException("user not found"));
        DisplayDto displayDto=new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());

        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {

        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("user not found"));
        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(obj.getValue().equals(user.getName()))
            {
                return "Cannot be same";
            }
            user.setName(obj.getValue());
            History h=new History();
            h.setDescription("User "+user.getUsername()+" Changes his name to:"+obj.getValue());
            historyRepo.save(h);
        }
        else if (obj.getKey().equalsIgnoreCase("password"))
        {
            if(obj.getValue().equals(user.getPassword()))
            {
                return "Cannot be same";
            }
           user.setPassword(obj.getValue());
            History h=new History();
            h.setDescription("User "+user.getUsername()+" Changes his password");
            historyRepo.save(h);
        }
        else if(obj.getKey().equalsIgnoreCase("email"))
        {
            if(obj.getValue().equals(user.getEmail()))
            {
                return "Cannot be same";
            }
            User user2=userRepo.findByEmail(obj.getValue());
            if(user2 !=null)
            {
                return "Email Already Exists";
            }
            user.setEmail(obj.getValue());
            History h=new History();
            h.setDescription("User "+user.getUsername()+"  Changes his email to:"+obj.getValue());
            historyRepo.save(h);
        }
        else
        {
            return "Invalid Update";
        }
        userRepo.save(user);

        return "Update successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId)
    {
        History h=new History();
        h.setDescription("Admin "+adminId+" created user " +user.getUsername());
        userRepo.save(user);
        if(user.getBalance()>0)
        {
            User user2=userRepo.findByUsername(user.getUsername());
            Transaction t=new Transaction();
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setUserId(user2.getId());
            t.setDescription("Rs"+user.getBalance()+"Deposit successful");
            transactionRepo.save(t);
        }
        historyRepo.save(h);

        return "Added successfully";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy,@RequestParam String order )
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort=Sort.by(sortBy).descending();
        }
        else
        {
            sort=Sort.by(sortBy).ascending();
        }

        return userRepo.findAllByRole("customer",sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

    @DeleteMapping("delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId, @PathVariable int adminId)
    {
        User user=userRepo.findById(userId).orElseThrow(()->new RuntimeException("user not found"));
        if(user.getBalance()>0)
        {
            return "balance is not zero";
        }
        History h=new History();
        h.setDescription("Admin "+adminId+" deleted user " +user.getUsername());
        historyRepo.save(h);
        userRepo.delete(user);
        return "Deleted successfully";
    }
}





