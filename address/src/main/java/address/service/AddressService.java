package address.service;

import model.Address;
import model.Result;

/**
 * 收货地址服务接口
 */
public interface AddressService {
    //新增地址
    Result addAddress(Address address);
    //删除地址
    Result deleteAddress(Integer uid, Integer id);
    //更新地址
    Result updateAddress(Integer uid, Address address);
    //获取用户地址列表
    Result listAddress(Integer uid);
    //设置默认地址
    Result setDefault(Integer uid, Integer id);
    //根据id获取地址详情
    Result getAddressById(Integer uid, Integer id);
}
