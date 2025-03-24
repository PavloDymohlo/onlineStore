package ua.dymohlo.onlineStore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailedResponse {
    private String email;
    private String fullName;
    private String role;
    private List<OrderSummaryResponse> orders;
}