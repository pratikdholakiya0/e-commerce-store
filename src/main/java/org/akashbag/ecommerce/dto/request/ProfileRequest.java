package org.akashbag.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.akashbag.ecommerce.model.Address;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    private String userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String about;
    private List<Address> address = new ArrayList<>();
    private String contactNo;
}
