package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransacationController
{
    @Autowired
    UserRepo userRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj)
    {
        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not found"));
        if(obj.getAmount()==0)
        {
            return "Enter Valid Amount";
        }
        double newbalance=user.getBalance()+obj.getAmount();
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction t=new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newbalance);
        t.setUserId(user.getId());
        t.setDescription("Rs"+obj.getAmount()+"Deposit successful");
        transactionRepo.save(t);
        return "Deposit successful";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj)
    {
        User user=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not found"));
        double newbalance=user.getBalance()-obj.getAmount();
        if(newbalance<0)
        {
            return "balance insufficient";
        }
        user.setBalance(newbalance);
        userRepo.save(user);

        Transaction t=new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newbalance);
        t.setUserId(user.getId());
        t.setDescription("Rs"+obj.getAmount()+"Withdrawal successful");
        transactionRepo.save(t);
        return "Withdrawal successful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj)
    {
        User sender=userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not found"));
        User rec=userRepo.findByUsername(obj.getUsername());
        if(rec==null)
        {
            return "username not found";
        }
        if(obj.getAmount()<1)
        {
            return "Invalid amount";
        }
        if(sender.getId()== rec.getId())
        {
            return "Self transaction is not allowed";
        }
        double sbalance=sender.getBalance()-obj.getAmount();
        double rbalance=rec.getBalance()+obj.getAmount();
        if(sbalance<0)
        {
            return "insufficient Balance";
        }

        sender.setBalance(sbalance);
        rec.setBalance(rbalance);

        userRepo.save(sender);
        userRepo.save(rec);

        Transaction t1=new Transaction();
        Transaction t2=new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs"+obj.getAmount()+"Sent to user:"+obj.getUsername());
        t1.setUserId(obj.getId());

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rbalance);
        t2.setDescription("Rs"+obj.getAmount()+"received by user:"+sender.getUsername());
        t2.setUserId(rec.getId());

        transactionRepo.save(t1);
        transactionRepo.save(t2);
        return "Transfer successfully";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable Integer id)
    {
        return transactionRepo.findAllByUserId(id);
    }
}


