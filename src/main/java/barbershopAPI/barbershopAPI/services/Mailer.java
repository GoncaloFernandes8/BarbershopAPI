// Mailer.java
package barbershopAPI.barbershopAPI.services;

import jakarta.activation.DataSource;
import jakarta.activation.FileTypeMap;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.entities.ServiceEntity;
import barbershopAPI.barbershopAPI.entities.Barber;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class Mailer {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:no-reply@barbearia.local}")
    String fromEmail;

    @Value("${MAIL_FROM_NAME:Barbearia}")
    String fromName;

    @Value("${FRONTEND_BASE_URL:https://example.com}")
    String frontendBaseUrl;

    private static final ZoneId TZ = ZoneId.of("Europe/Lisbon");
    private static final Locale PT = new Locale("pt", "PT");

    @Async
    public void sendAppointmentConfirmation(String to, Appointment appt, ServiceEntity svc, Barber barber) {
        try {
            var whenLisbon = appt.getStartsAt().atZoneSameInstant(TZ);
            var endsLisbon = appt.getEndsAt() == null ? null : appt.getEndsAt().atZoneSameInstant(TZ);

            var dateHuman = whenLisbon.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'às' HH:mm", PT));
            var subject = "✅ Marcação confirmada – " + dateHuman;

            var successUrl = frontendBaseUrl.replaceAll("/+$", "")
                    + "/sucesso/" + appt.getId().toString();

            // 1) Construir HTML + plain-text
            String html = buildHtmlEmail(appt, svc, barber, dateHuman, successUrl);
            String text = buildPlainEmail(appt, svc, barber, dateHuman, successUrl);

            // 2) Mime com HTML (e alternativa texto)
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html); // plain + html

            // 3) (Opcional) .ics “Adicionar ao calendário”
            byte[] ics = buildIcs(appt, svc, whenLisbon, endsLisbon);
            DataSource ds = new ByteArrayDataSource(ics, "text/calendar; charset=UTF-8");
            helper.addAttachment("marcacao-" + appt.getId() + ".ics", ds);

            mailSender.send(mime);
            log.info("Email de confirmação enviado para {} (appt {})", to, appt.getId());
        } catch (Exception ex) {
            log.warn("Falha ao enviar email de confirmação para {} (appt {}): {}",
                    to, appt.getId(), ex.getMessage(), ex);
        }
    }

    private String buildHtmlEmail(Appointment appt, ServiceEntity svc, Barber barber, String dateHuman, String successUrl) {
        // HTML de email: layout a 600px, tabelas + CSS inline (compatível com Gmail/Outlook)
        // Usa a tua cor accent #C3FF5A
        String accent = "#C3FF5A";
        String id = appt.getId().toString();
        String notes = appt.getNotes() == null ? "" : appt.getNotes();

        return """
    <!doctype html>
    <html lang="pt">
    <head>
      <meta charset="utf-8">
      <meta name="x-apple-disable-message-reformatting">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Marcação confirmada</title>
      <style>
        /* Para clientes que respeitam <style> */
        body{margin:0;background:#f5f7fb;color:#0b0c0f}
        .container{width:100%%;max-width:600px;margin:0 auto;background:#ffffff}
        .header{padding:16px 24px;background:#111111;color:#ffffff}
        .brand{font-weight:800;font-family:Arial,Helvetica,sans-serif}
        .content{padding:24px}
        h1{margin:0 0 8px 0;font-family:Arial,Helvetica,sans-serif}
        p,td,span,div{font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:1.5}
        .card{border:1px solid #e6e9f0;border-radius:12px;padding:16px;background:#ffffff}
        .row{padding:6px 0;border-bottom:1px solid #f0f2f7}
        .row:last-child{border-bottom:0}
        .label{color:#6b7280;display:inline-block;min-width:120px}
        .cta{display:inline-block;padding:12px 18px;border-radius:10px;text-decoration:none;background:%1$s;color:#111111;font-weight:800}
        .muted{color:#6b7280}
        .preheader{display:none!important;visibility:hidden;opacity:0;color:transparent;height:0;width:0}
        .footer{padding:16px 24px;color:#6b7280;font-size:12px}
      </style>
    </head>
    <body>
      <!-- Preheader (aparece ao lado do assunto) -->
      <div class="preheader">Vemo-nos %2$s. Detalhes da tua marcação dentro.</div>

      <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f5f7fb">
        <tr><td align="center">
          <table role="presentation" cellspacing="0" cellpadding="0" border="0" class="container">
            <tr>
              <td class="header">
                <div class="brand">Barbearia</div>
              </td>
            </tr>
            <tr>
              <td class="content">
                <h1>Marcação confirmada 🎉</h1>
                <p class="muted">Enviámos os detalhes abaixo e podes ver a marcação online quando quiseres.</p>

                <div class="card">
                  <div class="row"><span class="label">Quando</span> <span>%2$s</span></div>
                  <div class="row"><span class="label">Serviço</span> <span>%3$s (%4$d min)</span></div>
                  <div class="row"><span class="label">Barbeiro</span> <span>%5$s</span></div>
                  <div class="row"><span class="label">Referência</span> <span>#%6$s</span></div>
                  %7$s
                </div>

                <p style="margin:18px 0 0">
                  <a href="%8$s" class="cta" target="_blank" rel="noopener">Ver marcação</a>
                </p>

                <p class="muted" style="margin-top:18px">Se precisares de alterar a marcação, responde a este email.</p>
              </td>
            </tr>
            <tr>
              <td class="footer">
                © Barbearia · Rua Principal, 123 · (+351) 900 000 000
              </td>
            </tr>
          </table>
        </td></tr>
      </table>
    </body>
    </html>
    """.formatted(
                accent,                            // %1$s
                dateHuman,                         // %2$s
                svc.getName(),                     // %3$s
                svc.getDurationMin(),              // %4$d
                barber.getName(),                  // %5$s
                id,                                // %6$s
                notes.isBlank() ? "" : "<div class=\"row\"><span class=\"label\">Notas</span> <span>" + escapeHtml(notes) + "</span></div>", // %7$s
                successUrl                         // %8$s
        );
    }

    private String buildPlainEmail(Appointment appt, ServiceEntity svc, Barber barber, String dateHuman, String successUrl) {
        return """
      Marcação confirmada

      • Data e hora: %s
      • Serviço: %s (%d min)
      • Barbeiro: %s
      • Nº da marcação: #%s

      Ver online: %s

      Até já!
      """.formatted(
                dateHuman, svc.getName(), svc.getDurationMin(),
                barber.getName(), appt.getId().toString(), successUrl
        );
    }

    private byte[] buildIcs(Appointment appt, ServiceEntity svc, ZonedDateTime startLisbon, ZonedDateTime endLisbon) {
        // Evento em UTC para interoperabilidade
        var startUtc = startLisbon.withZoneSameInstant(ZoneOffset.UTC);
        var endUtc   = (endLisbon == null ? startLisbon.plusMinutes(svc.getDurationMin()) : endLisbon).withZoneSameInstant(ZoneOffset.UTC);

        String uid = appt.getId().toString() + "@barbearia";
        String summary = "Barbearia – " + svc.getName();
        String desc = "Marcação #" + appt.getId();

        String ics = """
      BEGIN:VCALENDAR
      VERSION:2.0
      PRODID:-//Barbearia//Appointments//PT
      CALSCALE:GREGORIAN
      METHOD:PUBLISH
      BEGIN:VEVENT
      UID:%s
      DTSTAMP:%sZ
      DTSTART:%sZ
      DTEND:%sZ
      SUMMARY:%s
      DESCRIPTION:%s
      END:VEVENT
      END:VCALENDAR
      """.formatted(
                uid,
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(ZonedDateTime.now(ZoneOffset.UTC)),
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(startUtc),
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(endUtc),
                escapeIcs(summary),
                escapeIcs(desc)
        );

        return ics.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeHtml(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
    private String escapeIcs(String s){
        return s.replace("\\","\\\\").replace("\n","\\n").replace(",","\\,").replace(";","\\;");
    }




    // services/Mailer.java (adiciona este método)
    public void sendEmailVerification(String to, String verifyLink){
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(to);
            helper.setSubject("Confirma o teu email · Barbearia");

            var html = """
      <div style="font-family:Arial,Helvetica,sans-serif">
        <h2>Confirma o teu email</h2>
        <p>Para concluíres o registo, confirma o teu email clicando no botão:</p>
        <p><a href="%1$s" style="background:#C3FF5A;color:#111;text-decoration:none;
              padding:12px 18px;border-radius:10px;display:inline-block;font-weight:700">
              Confirmar email</a></p>
        <p style="color:#666">Se não fores tu, ignora esta mensagem.</p>
      </div>
    """.formatted(verifyLink);

            helper.setText("Confirma o teu email: " + verifyLink, html);
            mailSender.send(mime);
        } catch (Exception ex) {
            log.warn("Falha a enviar email de verificação para {}: {}", to, ex.getMessage(), ex);
        }
    }



}
