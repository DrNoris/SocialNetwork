package com.example.lab6.domain.validators;


import com.example.lab6.domain.Utilizator;

public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        if (entity.getLastName().equals(""))
            throw new ValidationException("Invalid name");
        if (entity.getId() == 0)
            throw new ValidationException("Invalid ID");
        if(entity.getFirstName().equals(""))
            throw new ValidationException("Invalid name");
    }
}
