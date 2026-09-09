package com.example.demo.repository;

import com.example.demo.entity.TramTrungChuyenHub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramTrungChuyenHubRepository extends JpaRepository<TramTrungChuyenHub, Long> {
    List<TramTrungChuyenHub> findByTinhThanh(String tinhThanh);
}
