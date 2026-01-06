package org.akashbag.ecommerce.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "profiles")
public class Profile {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "User ID is mandatory")
    private String userId;

    @Indexed(unique = true)
    @NotBlank
    private String username;
    private String profileUrl;
    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    private String about;

    @NotNull(message = "Address list cannot be null")
    private List<Address> address = new ArrayList<>();

    @NotBlank(message = "Contact number is mandatory")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid contact number")
    @Indexed(unique = true)
    private String contactNo;

    @Builder.Default
    private boolean isPhoneVerified = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
