package com.github.tehilim.psalmproject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

@RestController
@RequestMapping("/BAG")
public class BAG {

    private final Map<String, String> bagRepository = new ConcurrentSkipListMap<>();

    private static final String HEADER = "bG; eG; tR; gegevens\n";

    @RequestMapping(path = "/{bagID}", method = RequestMethod.GET)
    public ResponseEntity<String> getBAGObject(@PathVariable String bagID) {
        String data = bagRepository.get(bagID);
        if (data == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HEADER + data, HttpStatus.OK);
    }

    @RequestMapping(path = "/{bagID}", method = RequestMethod.POST)
    public ResponseEntity<String> postBAGObject(@PathVariable String bagID, @RequestBody String body) {
        String current = bagRepository.get(bagID);
        if (current == null) {
            current = body;
        } else {
            current += "\n" + body;
        }
        bagRepository.put(bagID, current);
        return new ResponseEntity<>(current, HttpStatus.OK);
    }

    @RequestMapping(path = "/{bagID}", method = RequestMethod.PUT)
    public ResponseEntity<String> putBAGObject(@PathVariable String bagID, @RequestBody String body) {
        bagRepository.put(bagID, body);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

}
