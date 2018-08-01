package com.circulosiete.k8s.operator.metabase.watch;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.*;
import io.micronaut.context.annotation.Context;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Singleton
@Context
public class ConfigMapWatcher {
  private AtomicLong counter = new AtomicLong(0);


  public void foo() throws IOException {
    /*ApiClient client = Config.defaultClient();
    client.getHttpClient().setReadTimeout(60, TimeUnit.SECONDS);
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Watch<V1Namespace> watch =
      Watch.createWatch(
        client,
        api.listNamespaceCall(
          null, null, null, null, null, 50, null, null, Boolean.TRUE, null, null),
        new TypeToken<Watch.Response<V1Namespace>>() {}.getType());

    try {
      for (Watch.Response<V1Namespace> item : watch) {
        System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
      }
    } finally {
      watch.close();
    }*/
    /*ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Watch<V1ConfigMap> watch = Watch.createWatch(
      client,
      api.listConfigMapForAllNamespacesCall(null, null, null, null, null, null, null, null, Boolean.TRUE, null, null),
      new TypeToken<Watch.Response<V1ConfigMap>>() {
      }.getType());

    for (Watch.Response<V1ConfigMap> item : watch) {
      System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
    }*/
  }

  @PostConstruct
  public void fooRedHat() throws InterruptedException {
    System.out.println("Iniciando hilo");
    final CountDownLatch closeLatch = new CountDownLatch(1);
    Config config = Config.autoConfigure(null);

    while (true) {
      try (final KubernetesClient client = new DefaultKubernetesClient(config)) {


        try (Watch watch = client.configMaps().inAnyNamespace().watch(new Watcher<ConfigMap>() {
          @Override
          public void eventReceived(Watcher.Action action, ConfigMap resource) {
            //resource.getMetadata().getCreationTimestamp()
            //log.info("{}: {}", action, resource.getMetadata().getResourceVersion());
            String s = resource.getMetadata().getNamespace() + ":" + resource.getMetadata().getName();
            System.out.println("Iteration: " + counter.intValue());
            System.out.println("CM: " + s);
            System.out.println("action " + action);
          }

          @Override
          public void onClose(KubernetesClientException cause) {
            log.debug("Watcher onClose");
            if (cause != null) {
              log.error(cause.getMessage(), cause);
              System.err.println(cause.getMessage());
              closeLatch.countDown();
            }
          }
        })) {
          closeLatch.await(60, TimeUnit.SECONDS);
        } catch (KubernetesClientException | InterruptedException e) {
          log.error("Could not watch resources", e);
        }
      } catch (Exception e) {
        e.printStackTrace();
        log.error(e.getMessage(), e);

        Throwable[] suppressed = e.getSuppressed();
        if (suppressed != null) {
          for (Throwable t : suppressed) {
            log.error(t.getMessage(), t);
          }
        }
      }
      Thread.sleep(1000l);
      counter.incrementAndGet();
    }
  }
}
