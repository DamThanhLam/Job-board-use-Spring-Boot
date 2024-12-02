package vn.edu.iuh.fit.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.backend.models.Address;
import vn.edu.iuh.fit.backend.repositories.AddressRepository;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;
    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public void deleteById(Long id) {
        addressRepository.deleteById(id);
    }
}
