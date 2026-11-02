package it.meridiana.api;

import it.meridiana.common.ApiException;
import it.meridiana.domain.Item;
import it.meridiana.repo.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemRepository items;

    public ItemController(ItemRepository items) {
        this.items = items;
    }

    @GetMapping
    public List<Api.ItemView> list() {
        return items.findAll().stream().map(Mapper::item).toList();
    }

    @GetMapping("/{id}")
    public Api.ItemView get(@PathVariable String id) {
        return Mapper.item(load(id));
    }

    @PostMapping
    public Api.ItemView create(@RequestBody Api.ItemView view) {
        Item i = new Item();
        Mapper.copyItem(i, view);
        return Mapper.item(items.save(i));
    }

    @PutMapping("/{id}")
    public Api.ItemView update(@PathVariable String id, @RequestBody Api.ItemView view) {
        Item i = load(id);
        Mapper.copyItem(i, view);
        return Mapper.item(items.save(i));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        items.delete(load(id));
    }

    private Item load(String id) {
        return items.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Articolo non trovato"));
    }
}
