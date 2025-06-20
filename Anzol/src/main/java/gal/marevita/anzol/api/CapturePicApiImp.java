package gal.marevita.anzol.api;


import gal.marevita.anzol.service.CapturePicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.InstanceNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/images")
public class CapturePicApiImp {

  @Autowired
  private CapturePicService capturePicService;


  @PostMapping("/new")
  public ResponseEntity newImage(@RequestParam("file") MultipartFile file) {
    try {
      return ResponseEntity.ok(capturePicService.saveCapturePic(file));
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @GetMapping("/{userName}/{imgID}")
  public ResponseEntity getImage(@PathVariable String userName, @PathVariable String imgID) {
    try {

      Path file = capturePicService.getCapturePic(userName, imgID);

      Resource resource = new UrlResource(file.toUri());

      if (!resource.exists()) throw new InstanceNotFoundException("Sin imaxe de perfil");

      String contentType = Files.probeContentType(file);
      if (contentType == null) {
        contentType = "application/octet-stream"; // fallback xenérico
      }

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(contentType))
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
          .body(resource);

    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }
}

