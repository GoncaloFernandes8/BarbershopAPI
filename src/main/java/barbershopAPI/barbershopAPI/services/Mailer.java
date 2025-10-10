package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.entities.Barber;
import barbershopAPI.barbershopAPI.entities.ServiceEntity;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Mailer.java
@Service
@RequiredArgsConstructor
public class Mailer {
    private final JavaMailSender mailSender;
    @Value("${MAIL_FROM:no-reply@barbearia.local}") String from;
    @Async
    public void sendAppointmentConfirmation(String to, Appointment appt, ServiceEntity svc, Barber barber) {
        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Confirmação da marcação #" + appt.getId());

        var tz = ZoneId.of("Europe/Lisbon");
        var when = appt.getStartsAt().atZoneSameInstant(tz)
                .format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'às' HH:mm", new Locale("pt", "PT")));

        msg.setText("""
  Olá,

  A tua marcação foi confirmada:

  • Data e hora: %s
  • Serviço: %s (%d min)
  • Barbeiro: %s
  • Nº da marcação: #%s

  Até já!
  """.formatted(
                when,                 // String
                svc.getName(),        // String
                svc.getDurationMin(), // int -> %d está ok
                barber.getName(),     // String
                appt.getId().toString() // UUID -> %s
        ));

        mailSender.send(msg);
    }
}
