package com.myTesi.aloisioUmberto.data.dao;

import com.myTesi.aloisioUmberto.data.entities.Bar.ColorBar;
import com.myTesi.aloisioUmberto.dto.ColorBarDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorBarRepository extends JpaRepository<ColorBar, String> {

    List<ColorBar> findAllByUserId(String userId);

    Optional<ColorBarDto> findByIdAndUserId(String id, @NotNull String userId);
}