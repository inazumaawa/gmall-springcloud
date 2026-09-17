package address.service.impl;

import address.mapper.AddressMapper;
import address.service.AddressService;
import model.Address;
import model.Result;
import model.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 收货地址服务实现类
 */
@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressMapper addressMapper;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    @Override
    public Result addAddress(Address address) {
        if (!isValidPhone(address.getPhone())) {
            return Result.failure(ResultCodeEnum.FAIL, "手机号格式不正确");
        }
        List<Address> addressList = addressMapper.findByUid(address.getUid());
        // 如果用户没有地址，默认设置为默认地址
        if (addressList.isEmpty()) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        addressMapper.insertAddress(address);
        return Result.success("添加地址成功");
    }

    @Override
    public Result deleteAddress(Integer uid, Integer id) {
        // 归属校验：确保地址属于当前用户
        Address addr = addressMapper.findById(id);
        if (addr == null || !addr.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "地址不存在或无权操作");
        }
        addressMapper.deleteAddress(id);
        return Result.success("删除地址成功");
    }

    @Override
    public Result updateAddress(Integer uid, Address address) {
        if (!isValidPhone(address.getPhone())) {
            return Result.failure(ResultCodeEnum.FAIL, "手机号格式不正确");
        }
        // 归属校验：确保地址属于当前用户
        Address addr = addressMapper.findById(address.getId());
        if (addr == null || !addr.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "地址不存在或无权操作");
        }
        addressMapper.updateAddress(address);
        return Result.success("更新地址成功");
    }

    @Override
    public Result listAddress(Integer uid) {
        List<Address> addressList = addressMapper.findByUid(uid);
        return Result.success(addressList);
    }

    @Override
    public Result setDefault(Integer uid, Integer id) {
        // 归属校验：确保地址属于当前用户
        Address addr = addressMapper.findById(id);
        if (addr == null || !addr.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "地址不存在或无权操作");
        }
        addressMapper.clearDefaultByUid(uid);
        addressMapper.setDefault(id);
        return Result.success("设置默认地址成功");
    }

    @Override
    public Result getAddressById(Integer uid, Integer id) {
        Address addr = addressMapper.findById(id);
        if (addr == null || !addr.getUid().equals(uid)) {
            return Result.failure(ResultCodeEnum.FAIL, "地址不存在或无权操作");
        }
        return Result.success(addr);
    }
}
