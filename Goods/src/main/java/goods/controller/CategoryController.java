package goods.controller;

import goods.mapper.CategoryMapper;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class CategoryController {
    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping("/category/list")
    public Result listCategories() {
        return Result.success(categoryMapper.findAll());
    }
}
