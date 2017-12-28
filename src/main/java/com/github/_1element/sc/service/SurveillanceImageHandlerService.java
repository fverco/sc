package com.github._1element.sc.service; //NOSONAR

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.github._1element.sc.domain.SurveillanceImage;
import com.github._1element.sc.events.ImageReceivedEvent;
import com.github._1element.sc.events.PushNotificationEvent;
import com.github._1element.sc.events.RemoteCopyEvent;
import com.github._1element.sc.properties.ImageProperties;
import com.github._1element.sc.repository.SurveillanceImageRepository;

/**
 * Service to handle surveillance images.
 */
@Service
public class SurveillanceImageHandlerService {

  private SurveillanceImageRepository imageRepository;

  private FileService fileService;

  private ThumbnailService thumbnailService;

  private ImageProperties imageProperties;

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  private static final String SEPARATOR = "-";

  private static final Logger LOG = LoggerFactory.getLogger(SurveillanceImageHandlerService.class);

  /**
   * Constructor.
   *
   * @param imageRepository the image repository
   * @param fileService the file service
   * @param thumbnailService the thumbnail service
   * @param imageProperties the image properties
   */
  @Autowired
  public SurveillanceImageHandlerService(SurveillanceImageRepository imageRepository, FileService fileService,
                                         ThumbnailService thumbnailService, ImageProperties imageProperties) {
    this.imageRepository = imageRepository;
    this.fileService = fileService;
    this.thumbnailService = thumbnailService;
    this.imageProperties = imageProperties;
  }

  /**
   * Event listener for image received events.
   * Will create thumbnail and database entry.
   *
   * @param imageReceivedEvent image received event
   */
  @Async
  @EventListener
  public void handleImageReceivedEvent(ImageReceivedEvent imageReceivedEvent) {
    // source and destination files
    Path sourcePath = fileService.getPath(imageReceivedEvent.getFileName());
    StringBuilder destinationFileName = new StringBuilder(imageProperties.getStorageDir());
    destinationFileName.append(fileService.getUniquePrefix()).append(SEPARATOR);
    destinationFileName.append(imageReceivedEvent.getSource().getId()).append(SEPARATOR);
    destinationFileName.append(sourcePath.getFileName().toString());
    Path destinationPath = fileService.getPath(destinationFileName.toString());

    try {
      // move file from incoming ftp directory to final storage directory
      fileService.moveFile(sourcePath, destinationPath);

      // generate thumbnail
      thumbnailService.createThumbnail(destinationPath);

      // store image information in database
      SurveillanceImage image = new SurveillanceImage(destinationPath.getFileName().toString(),
          imageReceivedEvent.getSource().getId(), LocalDateTime.now());
      imageRepository.save(image);

      LOG.info("New surveillance image '{}' was received.", image.getFileName());

      // publish events to invoke remote copy and push notification
      eventPublisher.publishEvent(new RemoteCopyEvent(destinationFileName.toString()));
      eventPublisher.publishEvent(new PushNotificationEvent(imageReceivedEvent.getSource()));
    } catch (IOException exception) {
      LOG.error("Error while moving file from incoming ftp directory to final storage directory: '{}'",
          exception.getMessage());
    }
  }

}
