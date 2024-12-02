package vn.edu.iuh.fit.backend.models;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "street", length = 150)
    private String street;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "country", length = 30,columnDefinition = "SMALLINT")
    private CountryCode country = CountryCode.VN;

    @Column(name = "number", length = 20)
    private String number;

    @Column(name = "zipcode", length = 7)
    private String zipcode;
    public Address(String number, String street, String city, String zipcode, CountryCode country) {
        this.number = number;
        this.street = street;
        this.city = city;
        this.zipcode = zipcode;
        this.country = country;
    }
    public Address(){}
}