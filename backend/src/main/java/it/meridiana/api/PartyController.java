package it.meridiana.api;

import it.meridiana.common.ApiException;
import it.meridiana.domain.Party;
import it.meridiana.domain.PartyKind;
import it.meridiana.repo.PartyRepository;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/parties")
public class PartyController {
    private final PartyRepository parties;

    public PartyController(PartyRepository parties) {
        this.parties = parties;
    }

    @GetMapping
    public List<Api.PartyView> list(@RequestParam PartyKind kind) {
        return parties.findByKindOrderByNameAsc(kind).stream().map(Mapper::party).toList();
    }

    @GetMapping("/{id}")
    public Api.PartyView get(@PathVariable String id) {
        return Mapper.party(load(id));
    }

    @PostMapping
    public Api.PartyView create(@RequestBody Api.PartyView view) {
        if (view.name() == null || view.name().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nome obbligatorio");
        }
        Party p = new Party();
        Mapper.copyParty(p, view);
        if (p.getKind() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tipo anagrafica obbligatorio");
        }
        return Mapper.party(parties.save(p));
    }

    @PutMapping("/{id}")
    public Api.PartyView update(@PathVariable String id, @RequestBody Api.PartyView view) {
        Party p = load(id);
        Mapper.copyParty(p, view);
        return Mapper.party(parties.save(p));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        parties.delete(load(id));
    }

    private Party load(String id) {
        return parties.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Anagrafica non trovata"));
    }
}
