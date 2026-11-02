package it.meridiana.api;

import it.meridiana.domain.DocumentType;
import it.meridiana.service.DocumentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documents;

    public DocumentController(DocumentService documents) {
        this.documents = documents;
    }

    @GetMapping
    public List<Api.DocView> list(@RequestParam DocumentType type) {
        return documents.list(type);
    }

    @GetMapping("/{id}")
    public Api.DocView get(@PathVariable String id) {
        return documents.get(id);
    }

    @PostMapping
    public Api.DocView create(@RequestBody Api.DocView view) {
        return documents.create(view);
    }

    @PutMapping("/{id}")
    public Api.DocView update(@PathVariable String id, @RequestBody Api.DocView view) {
        return documents.update(id, view);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        documents.delete(id);
    }

    @PostMapping("/{id}/confirm")
    public Api.DocView confirm(@PathVariable String id) {
        return documents.confirm(id);
    }

    @PostMapping("/{id}/void")
    public Api.DocView voidDoc(@PathVariable String id) {
        return documents.voidDoc(id);
    }

    @PostMapping("/{id}/convert")
    public Api.DocView convert(@PathVariable String id, @RequestBody Api.ConvertRequest req) {
        return documents.convert(id, req.targetType());
    }

    @PostMapping("/{id}/einvoice")
    public Api.DocView einvoice(@PathVariable String id) {
        return documents.submitEinvoice(id);
    }
}
