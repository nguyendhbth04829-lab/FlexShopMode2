package com.example.demo.repository;

import com.example.demo.entity.BangGiaVanChuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BangGiaVanChuyenRepository extends JpaRepository<BangGiaVanChuyen, Long> {

    Optional<BangGiaVanChuyen> findFirstByDoiTac_MaDoiTacAndTuyenVanChuyen(Integer maDoiTac, String tuyenVanChuyen);

    List<BangGiaVanChuyen> findByTuyenVanChuyen(String tuyenVanChuyen);

    List<BangGiaVanChuyen> findByDoiTac_MaDoiTac(Integer maDoiTac);
}
