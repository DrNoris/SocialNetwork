package com.example.lab6.domain.validators;

import com.example.lab6.domain.Prietenie;

public class PrieteniValidator<T> implements Validator<Prietenie<T>> {

    @Override
    public void validate(Prietenie<T> entity) throws ValidationException {
        if (entity.getId().getLeft() instanceof Long && entity.getId().getRight() instanceof Long) {
            if ((Long) entity.getId().getLeft() == 0 || (Long) entity.getId().getRight() == 0) {
                throw new ValidationException("Invalid ID");
            }
        } else {
            throw new ValidationException("Invalid ID type. Expected Long.");
        }
    }
}

