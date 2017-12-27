package com.github._1element.sc.repository;

import com.github._1element.sc.SurveillanceCenterApplication;
import com.github._1element.sc.domain.Camera;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SurveillanceCenterApplication.class)
@WebAppConfiguration
public class CameraRepositoryTest {

  @Autowired
  private CameraRepository cameraRepository;

  @Test
  public void testFindAll() throws Exception {
    List<Camera> result = cameraRepository.findAll();

    assertEquals(4, result.size());
    assertEquals("testcamera1", result.get(0).getId());
    assertEquals("testcamera2", result.get(1).getId());
    assertEquals("testcamera3", result.get(2).getId());
    assertEquals("testcamera4", result.get(3).getId());
  }

  @Test
  public void testFindById() throws Exception {
    Camera camera1 = cameraRepository.findById("testcamera1");

    assertEquals("Front door", camera1.getName());
    assertEquals("192.168.1.50", camera1.getHost());
    assertEquals("username", camera1.getFtpUsername());
    assertEquals("password", camera1.getFtpPassword());
    assertEquals("https://localhost/cam1/snapshot", camera1.getSnapshotUrl());
    assertEquals("/tmp/camera1/", camera1.getFtpIncomingDirectory());
    assertTrue(camera1.isStreamEnabled());
    assertTrue(camera1.isSnapshotEnabled());

    Camera camera2 = cameraRepository.findById("testcamera2");

    assertEquals("Backyard", camera2.getName());
    assertEquals("192.168.1.51", camera2.getHost());
    assertEquals("user2", camera2.getFtpUsername());
    assertEquals("password2", camera2.getFtpPassword());
    assertEquals("https://localhost/cam2/snapshot", camera2.getSnapshotUrl());
    assertEquals("/tmp/camera2/", camera2.getFtpIncomingDirectory());
    assertTrue(camera2.isSnapshotEnabled());
    assertFalse(camera2.isStreamEnabled());
  }

  @Test
  public void testFindByFtpUsernameCamera1() throws Exception {
    Camera camera1 = cameraRepository.findByFtpUsername("username");

    assertEquals("testcamera1", camera1.getId());
    assertEquals("password", camera1.getFtpPassword());
    assertEquals("/tmp/camera1/", camera1.getFtpIncomingDirectory());
  }

  @Test
  public void testFindByFtpUsernameCamera2() throws Exception {
    Camera camera2 = cameraRepository.findByFtpUsername("user2");

    assertEquals("testcamera2", camera2.getId());
    assertEquals("password2", camera2.getFtpPassword());
    assertEquals("/tmp/camera2/", camera2.getFtpIncomingDirectory());
  }

  @Test
  public void testFindByFtpUsernameInvalid() throws Exception {
    assertNull(cameraRepository.findByFtpUsername("invalid-username"));
  }

  @Test
  public void testFindByFtpUsernameNull() throws Exception {
    assertNull(cameraRepository.findByFtpUsername(null));
  }

  @Test
  public void testFindAllWithSnapshotUrl() throws Exception {
    List<Camera> result = cameraRepository.findAllWithSnapshotUrl();

    assertEquals(3, result.size());
    assertTrue(result.stream().noneMatch(e -> "Camera without snapshot url".equals(e.getName())));
  }

}
