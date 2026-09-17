package address.controller;

import address.service.AddressService;
import model.Address;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收货地址控制器
 */
@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    //新增地址
    @PostMapping("/add")
    public Result addAddress(@RequestHeader("uid") Integer uid, @RequestBody Address address) {
        address.setUid(uid);
        return addressService.addAddress(address);
    }

    //删除地址
    @DeleteMapping("/delete")
    public Result deleteAddress(@RequestHeader("uid") Integer uid, @RequestParam Integer id) {
        return addressService.deleteAddress(uid, id);
    }

    //更新地址
    @PutMapping("/update")
    public Result updateAddress(@RequestHeader("uid") Integer uid, @RequestBody Address address) {
        return addressService.updateAddress(uid, address);
    }

    //获取地址列表
    @GetMapping("/list")
    public Result listAddress(@RequestHeader("uid") Integer uid) {
        return addressService.listAddress(uid);
    }

    //设置默认地址
    @PutMapping("/setdefault")
    public Result setDefault(@RequestHeader("uid") Integer uid, @RequestParam Integer id) {
        return addressService.setDefault(uid, id);
    }

    //查询单条地址详情
    @GetMapping("/detail")
    public Result detail(@RequestHeader("uid") Integer uid, @RequestParam Integer id) {
        return addressService.getAddressById(uid, id);
    }
}
