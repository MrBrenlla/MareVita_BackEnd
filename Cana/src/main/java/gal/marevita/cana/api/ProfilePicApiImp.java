package gal.marevita.cana.api;


import gal.marevita.cana.service.UserService;
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
@RequestMapping("/profile")
public class ProfilePicApiImp {

  @Autowired
  private UserService userService;


  @PutMapping("/pic")
  public ResponseEntity updateProfilePic(@RequestParam("file") MultipartFile file) {
    try {
      userService.updateProfilePic(file);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      return ExceptionManager.manage(e);
    }
  }

  @GetMapping("/pic/{userName}")
  public ResponseEntity getProfilePic(@PathVariable String userName) {
    try {

      Path file = userService.getProfilePic(userName);

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

