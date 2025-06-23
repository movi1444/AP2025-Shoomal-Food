package com.aut.shoomal.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BankInfoResponse {
    private String bankName, accountNumber;
    public BankInfoResponse() {}

    public BankInfoResponse(String bankName, String accountNumber)
    {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }

    @JsonProperty("bank_name")
    public String getBankName()
    {
        return bankName;
    }

    @JsonProperty("bank_name")
    public void setBankName(String bankName)
    {
        this.bankName = bankName;
    }

    @JsonProperty("account_number")
    public String getAccountNumber()
    {
        return accountNumber;
    }

    @JsonProperty("account_number")
    public void setAccountNumber(String accountNumber)
    {
        this.accountNumber = accountNumber;
    }
}