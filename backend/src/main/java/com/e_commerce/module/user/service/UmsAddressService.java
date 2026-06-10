package com.e_commerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.user.dto.UserAddressDto;
import com.e_commerce.module.user.entity.UmsAddress;
import com.e_commerce.module.user.mapper.UmsAddressMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UmsAddressService {
    @Autowired
    private UmsAddressMapper umsAddressMapper;

    public List<UmsAddress> listByUserId() {
        Long userId = ThreadLocalUtil.getUserId();
        return umsAddressMapper.selectList(new LambdaQueryWrapper<UmsAddress>().eq(UmsAddress::getUserId, userId));
    }

    public void add(UserAddressDto address) {
        Long userId = ThreadLocalUtil.getUserId();
        UmsAddress entity = new UmsAddress();
        BeanUtils.copyProperties(address, entity);
        entity.setUserId(userId);
        umsAddressMapper.insert(entity);
    }

    public void update(UserAddressDto address) {
        Long userId = ThreadLocalUtil.getUserId();
        umsAddressMapper.update(null, new LambdaUpdateWrapper<UmsAddress>()
                .eq(UmsAddress::getId, address.getId())
                .eq(UmsAddress::getUserId, userId)
                .set(UmsAddress::getReceiver, address.getReceiver())
                .set(UmsAddress::getPhone, address.getPhone())
                .set(UmsAddress::getProvince, address.getProvince())
                .set(UmsAddress::getCity, address.getCity())
                .set(UmsAddress::getArea, address.getArea())
                .set(UmsAddress::getDetail, address.getDetail()));
    }

    public void delete(Long id) {
        umsAddressMapper.deleteById(id);
    }

    @Transactional
    public void setDefault(Long addressId) {
        Long userId = ThreadLocalUtil.getUserId();
        umsAddressMapper.update(null, new LambdaUpdateWrapper<UmsAddress>()
                .eq(UmsAddress::getUserId, userId)
                .set(UmsAddress::getIsDefault, 0));
        umsAddressMapper.update(null, new LambdaUpdateWrapper<UmsAddress>()
                .eq(UmsAddress::getId, addressId)
                .set(UmsAddress::getIsDefault, 1));
    }
}
