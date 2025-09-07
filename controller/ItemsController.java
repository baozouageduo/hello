package com.tests.campuslostandfoundsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tests.campuslostandfoundsystem.entity.R;
import com.tests.campuslostandfoundsystem.entity.admins.ItemTypeCountDTO;
import com.tests.campuslostandfoundsystem.entity.items.Items;
import com.tests.campuslostandfoundsystem.entity.items.ItemsSelectionDTO;
import com.tests.campuslostandfoundsystem.service.items.ItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemsController {
    private final ItemsService itemsService;

    // ✅ 前端会调 POST /items/pages，这里补上 POST 版本
    @PreAuthorize("hasAnyRole('ADMIN','ITEM_ADMIN','STUDENT')")
    @PostMapping("/pages")
    public R<Page<Items>> pagesByPost(@RequestBody ItemsSelectionDTO dto){
        return R.success(itemsService.getAllItemsPages(dto));
    }

    // 也保留 GET /items/pages，方便直接访问
    @PreAuthorize("hasAnyRole('ADMIN','ITEM_ADMIN','STUDENT')")
    @GetMapping("/pages")
    public R<Page<Items>> pagesByGet(ItemsSelectionDTO dto){
        return R.success(itemsService.getAllItemsPages(dto));
    }

    // 你原有的 GET /items or /items/ 也当只读分页（可要可不要）
    @PreAuthorize("hasAnyRole('ADMIN','ITEM_ADMIN','STUDENT')")
    @GetMapping({"", "/"})
    public R<Page<Items>> getAllItemsPages(ItemsSelectionDTO dto){
        return R.success(itemsService.getAllItemsPages(dto));
    }

    // 只读统计
    @PreAuthorize("hasAnyRole('ADMIN','ITEM_ADMIN','STUDENT')")
    @GetMapping("/itemTypeCount")
    public R<ItemTypeCountDTO> getItemTypeCount(){
        return R.success(itemsService.getItemTypeCount());
    }

    // 写入受限
    @PreAuthorize("hasAnyRole('ADMIN','ITEM_ADMIN')")
    @PostMapping({"", "/"})
    public R<Void> insertItem(@RequestBody Items items){
        itemsService.insertItem(items);
        return R.success(null);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ITEM_ADMIN')")
    @PutMapping({"", "/"})
    public R<Void> updateItem(@RequestBody Items items){
        itemsService.updateItem(items);
        return R.success(null);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public R<Void> deleteItem(@PathVariable("id") Long id){
        itemsService.deleteItem(id);
        return R.success(null);
    }
}
