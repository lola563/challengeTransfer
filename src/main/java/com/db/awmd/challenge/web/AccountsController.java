package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.NegativeBalanceException;
import com.db.awmd.challenge.service.AccountsService;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
  
  @PutMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferAmount(@RequestBody @Valid TransferDTO transferOperation) {
    log.info("Transfer account from {} to {} amount {}", transferOperation.getAccountFromId(), transferOperation.getAccountToId(), transferOperation.getAmount());
    
    try {
    	Account accountFrom= this.accountsService.getAccount(transferOperation.getAccountFromId());
    	Account accountTo= this.accountsService.getAccount(transferOperation.getAccountToId());
    	if (null == accountFrom) {
    		return new ResponseEntity<>("Account id " + transferOperation.getAccountFromId() + " does not exists!", HttpStatus.BAD_REQUEST);
    	}
    	if (null == accountTo) {
    		return new ResponseEntity<>("Account id" + transferOperation.getAccountToId() + " does not exists!", HttpStatus.BAD_REQUEST);
    	}
    	
    	this.accountsService.transferAmount(transferOperation.getAccountFromId(), transferOperation.getAccountToId(), transferOperation.getAmount());
    	
    } catch (NegativeBalanceException nbe) {
      return new ResponseEntity<>(nbe.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

}
