package com.example.demo.repository;

import com.example.demo.entity.LichSuGiaBienThe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuGiaBienTheRepository extends JpaRepository<LichSuGiaBienThe, Long> {
    List<LichSuGiaBienThe> findByBienThe_MaBienTheOrderByNgayThayDoiDesc(Long maBienThe);
}
