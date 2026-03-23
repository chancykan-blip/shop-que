package com.myshop;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
@RestController
public class UserController {

    // 查询
    @GetMapping("/search")
    public List<String> search(
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam(required = false, defaultValue = "") String status) {
        return TestConnection.searchUsers(region, status);
    }

    // 新增用户
    @PostMapping("/user/add")
    public String addUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String region,
            @RequestParam String status) {
        return TestConnection.addUser(name, email, region, status);
    }

    // 删除用户
    @DeleteMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        return TestConnection.deleteUser(id);
    }

    // 编辑用户
    @PutMapping("/user/update/{id}")
    public String updateUser(
            @PathVariable int id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String region,
            @RequestParam String status) {
        return TestConnection.updateUser(id, name, email, region, status);
    }

    @PostMapping("/user/import")
    public String importUsers(@RequestParam("file") MultipartFile file) {
        return TestConnection.importFromCSV(file);
    }
    // 各地区用户数量
    @GetMapping("/stats/region")
    public List<String> statsByRegion() {
        return TestConnection.statsByRegion();
    }

    // 用户状态分布
    @GetMapping("/stats/status")
    public List<String> statsByStatus() {
        return TestConnection.statsByStatus();
    }

    // 每月订单金额
    @GetMapping("/stats/monthly")
    public List<String> statsMonthly() {
        return TestConnection.statsMonthly();
    }
}