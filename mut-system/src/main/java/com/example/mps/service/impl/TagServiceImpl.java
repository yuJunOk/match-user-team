package com.example.mps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mps.mapper.TagMapper;
import com.example.mps.pojo.domain.TagDo;
import com.example.mps.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author pengYuJun
 * @description 针对表【tb_tag(标签表)】的数据库操作Service实现
 * @createDate 2025-04-14 17:12:06
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagDo>
        implements TagService {

}
