package org.codenot.househub.finance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank/v1")
public class BankController {
    @GetMapping("/balance")
    public ResponseEntity<AccountBalanceDTO> getBalance() {

        String baseUrl = "https://simulator-api.db.com:443/gw/dbapi/banking/cashAccounts/v2";


        return ResponseEntity.ok(new AccountBalanceDTO("1234567890", "1000.00"));
    }

    public record AccountBalanceDTO(String accountNumber, String balance) {}
}
