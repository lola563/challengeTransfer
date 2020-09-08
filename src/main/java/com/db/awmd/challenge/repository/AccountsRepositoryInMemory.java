package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.NegativeBalanceException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();


  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

  
  @Override
  public int transferAmount(String accountFromId, String accountToId, BigDecimal amount) throws NegativeBalanceException {
	  
	  if (null != accounts.get(accountFromId) && null != accounts.get(accountFromId) && amount.compareTo(BigDecimal.ZERO) > 0) {
		  final AtomicInteger balanceFrom = new AtomicInteger (accounts.get(accountFromId).getBalance().intValue());
		  final AtomicInteger balanceTo = new AtomicInteger (accounts.get(accountToId).getBalance().intValue());
		  
		  if (balanceFrom.get() - amount.intValue() < 0){
			      throw new NegativeBalanceException(
			    	        "Balance of account id " + accounts.get(accountFromId).getAccountId() + " should not be negative!");
		  }
		  
		  accounts.replace(accountFromId, new Account(accountFromId, new BigDecimal(balanceFrom.get() - amount.intValue())));
		  accounts.replace(accountToId, new Account(accountFromId, new BigDecimal(balanceTo.addAndGet(amount.intValue()))));
		 
		  return 1;
	  }else {
		  return 0;
	  }
		
  }

}
