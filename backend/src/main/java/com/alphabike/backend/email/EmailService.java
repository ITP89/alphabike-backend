package com.alphabike.backend.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.mail.from:AlphaBike <no-reply@alphabike.com>}")
    private String fromEmail;

    public void enviarVerificacionEmail(String destinatario, String nombre, String token) {
        String enlace = frontendUrl + "/verificar-email?token=" + token;
        String asunto = "Activa tu cuenta en AlphaBike";

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f4f4f5; margin: 0; padding: 24px; color: #18181b;">
              <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); border: 1px solid #e4e4e7;">
                <tr>
                  <td style="background-color: #09090b; padding: 32px; text-align: center; border-bottom: 4px solid #dc2626;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 900; letter-spacing: -0.5px;">ALPHABIKE <span style="color: #ef4444;">PRO</span></h1>
                    <p style="color: #a1a1aa; margin: 6px 0 0 0; font-size: 13px; font-weight: 500;">Taller Especializado y Tienda de Ciclismo</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding: 36px 32px;">
                    <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 800; color: #09090b;">¡Hola, %s!</h2>
                    <p style="font-size: 14px; line-height: 1.6; color: #52525b; margin-bottom: 24px;">
                      Gracias por unirte a AlphaBike. Para completar tu registro y acceder a compras de repuestos, agendamiento de talleres mecánicos y seguimiento en tiempo real, confirma tu dirección de correo electrónico:
                    </p>
                    <div style="text-align: center; margin: 32px 0;">
                      <a href="%s" style="background-color: #dc2626; color: #ffffff; padding: 14px 28px; font-size: 14px; font-weight: 800; text-decoration: none; border-radius: 10px; display: inline-block; box-shadow: 0 4px 10px rgba(220, 38, 38, 0.35);">
                        Activar Mi Cuenta
                      </a>
                    </div>
                    <p style="font-size: 12px; line-height: 1.5; color: #71717a; margin-top: 24px; border-top: 1px solid #f4f4f5; pt: 16px;">
                      Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:<br>
                      <a href="%s" style="color: #dc2626; word-break: break-all;">%s</a>
                    </p>
                    <p style="font-size: 11px; color: #a1a1aa; margin-top: 20px;">
                      Este enlace expirará en 24 horas. Si no creaste esta cuenta, puedes desestimar este mensaje.
                    </p>
                  </td>
                </tr>
                <tr>
                  <td style="background-color: #f4f4f5; padding: 20px; text-align: center; font-size: 11px; color: #71717a;">
                    © 2026 AlphaBike Workshop & Store. Todos los derechos reservados.
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(nombre, enlace, enlace, enlace);

        enviarHtml(destinatario, asunto, html, "Verificación de Correo", enlace, token);
    }

    public void enviarRecuperacionPassword(String destinatario, String nombre, String token) {
        String enlace = frontendUrl + "/restablecer-password?token=" + token;
        String asunto = "Recuperación de contraseña - AlphaBike";

        String html = """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f4f4f5; margin: 0; padding: 24px; color: #18181b;">
              <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); border: 1px solid #e4e4e7;">
                <tr>
                  <td style="background-color: #09090b; padding: 32px; text-align: center; border-bottom: 4px solid #dc2626;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 900; letter-spacing: -0.5px;">ALPHABIKE <span style="color: #ef4444;">PRO</span></h1>
                    <p style="color: #a1a1aa; margin: 6px 0 0 0; font-size: 13px; font-weight: 500;">Seguridad y Recuperación de Cuenta</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding: 36px 32px;">
                    <h2 style="margin: 0 0 16px 0; font-size: 20px; font-weight: 800; color: #09090b;">Hola, %s</h2>
                    <p style="font-size: 14px; line-height: 1.6; color: #52525b; margin-bottom: 24px;">
                      Hemos recibido una solicitud para restablecer la contraseña de tu cuenta de AlphaBike. Haz clic en el botón a continuación para ingresar una nueva contraseña:
                    </p>
                    <div style="text-align: center; margin: 32px 0;">
                      <a href="%s" style="background-color: #09090b; color: #ffffff; padding: 14px 28px; font-size: 14px; font-weight: 800; text-decoration: none; border-radius: 10px; display: inline-block; box-shadow: 0 4px 10px rgba(0, 0, 0, 0.25);">
                        Restablecer Mi Contraseña
                      </a>
                    </div>
                    <p style="font-size: 12px; line-height: 1.5; color: #71717a; margin-top: 24px; border-top: 1px solid #f4f4f5; pt: 16px;">
                      Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:<br>
                      <a href="%s" style="color: #dc2626; word-break: break-all;">%s</a>
                    </p>
                    <p style="font-size: 11px; color: #a1a1aa; margin-top: 20px;">
                      Por motivos de seguridad, este enlace es de un solo uso y expirará en 30 minutos.<br>
                      Si tú no solicitaste este cambio, puedes ignorar este mensaje; tu contraseña actual no se modificará.
                    </p>
                  </td>
                </tr>
                <tr>
                  <td style="background-color: #f4f4f5; padding: 20px; text-align: center; font-size: 11px; color: #71717a;">
                    © 2026 AlphaBike Workshop & Store. Mensaje de seguridad automatizado.
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(nombre, enlace, enlace, enlace);

        enviarHtml(destinatario, asunto, html, "Recuperación de Contraseña", enlace, token);
    }

    private void enviarHtml(String destinatario, String asunto, String html, String tipo, String enlace, String token) {
        boolean enviadoPorSmtp = false;

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                helper.setFrom(fromEmail);
                helper.setTo(destinatario);
                helper.setSubject(asunto);
                helper.setText(html, true);

                mailSender.send(message);
                enviadoPorSmtp = true;
                log.info("Correo electrónico [{}] enviado exitosamente a {}", tipo, destinatario);
            } catch (Exception e) {
                log.warn("No se pudo enviar correo por SMTP ({}), registrando en modo simulado/consola: {}", e.getMessage(), enlace);
            }
        }

        if (!enviadoPorSmtp) {
            // Modo simulado / desarrollo para asegurar que ningún flujo se bloquee si no hay servidor SMTP en local
            log.info("\n=================================================================="
                    + "\n[ALPHABIKE SIMULADOR DE CORREO - LOCAL DEV]"
                    + "\nTipo:        " + tipo
                    + "\nDestinatario:" + destinatario
                    + "\nAsunto:      " + asunto
                    + "\nToken:       " + token
                    + "\nEnlace:      " + enlace
                    + "\n==================================================================");
        }
    }
}
