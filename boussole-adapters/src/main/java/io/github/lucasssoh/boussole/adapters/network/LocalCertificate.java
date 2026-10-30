package io.github.lucasssoh.boussole.adapters.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The certificate other devices reach Boussole with over HTTPS, made on this computer by the
 * {@code keytool} of the Java runtime. It names every address and name of the computer; one
 * missing (a new network) makes a new certificate. The keystore password stays beside it, readable
 * by its owner only.
 */
public record LocalCertificate(Path keystore, String password, String fingerprint) {

    private static final String ALIAS = "boussole";
    private static final String PASSWORD = "BOUSSOLE_KEYSTORE_PASSWORD";

    /** {@code hosts}: the IP addresses and names the certificate must name. */
    public static LocalCertificate ensure(Path folder, List<String> hosts) {
        Path keystore = folder.resolve("network.p12");
        Path secret = folder.resolve("network.secret");
        try {
            Files.createDirectories(folder);
            if (Files.exists(keystore) && Files.exists(secret)) {
                String password = Files.readString(secret, StandardCharsets.UTF_8).strip();
                X509Certificate certificate = certificate(keystore, password);
                if (names(certificate).containsAll(hosts)) {
                    return new LocalCertificate(keystore, password, fingerprint(certificate));
                }
            }
            String password = newPassword();
            Files.deleteIfExists(keystore);
            generate(keystore, password, hosts);
            Files.writeString(secret, password, StandardCharsets.UTF_8);
            restrict(secret);
            restrict(keystore);
            return new LocalCertificate(keystore, password, fingerprint(certificate(keystore, password)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("cannot read the network certificate " + keystore + ": " + e.getMessage(), e);
        }
    }

    /** The first four bytes of the SHA-256 fingerprint, to compare with the browser's: {@code 7F 3A 91 C4}. */
    public String shortFingerprint() {
        return fingerprint.substring(0, 11);
    }

    private static void generate(Path keystore, String password, List<String> hosts) throws IOException {
        Path keytool = Path.of(System.getProperty("java.home"), "bin", isWindows() ? "keytool.exe" : "keytool");
        List<String> san = new ArrayList<>(List.of("dns:localhost", "ip:127.0.0.1"));
        for (String host : hosts) {
            san.add((host.matches("[0-9.]+") ? "ip:" : "dns:") + host);
        }
        ProcessBuilder builder = new ProcessBuilder(keytool.toString(), "-genkeypair", "-alias", ALIAS,
                "-keyalg", "EC", "-groupname", "secp256r1", "-sigalg", "SHA256withECDSA",
                "-dname", "CN=Boussole", "-validity", "3650", "-ext", "san=" + String.join(",", san),
                "-storetype", "PKCS12", "-keystore", keystore.toString(), "-storepass:env", PASSWORD, "-keypass:env", PASSWORD);
        // The password goes through the environment: a command line shows in the list of processes.
        builder.environment().put(PASSWORD, password);
        Process process = builder.redirectErrorStream(true).start();
        try {
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) {
                throw new IOException("keytool could not make the network certificate: " + output.strip());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while making the network certificate", e);
        }
    }

    private static X509Certificate certificate(Path keystore, String password) throws IOException, GeneralSecurityException {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keystore)) {
            store.load(in, password.toCharArray());
        }
        return (X509Certificate) store.getCertificate(ALIAS);
    }

    private static Set<String> names(X509Certificate certificate) throws GeneralSecurityException {
        Set<String> names = new HashSet<>();
        Collection<List<?>> alternatives = certificate.getSubjectAlternativeNames();
        if (alternatives != null) {
            alternatives.forEach(entry -> names.add(String.valueOf(entry.get(1))));
        }
        return names;
    }

    private static String fingerprint(X509Certificate certificate) throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded());
        return HexFormat.ofDelimiter(" ").withUpperCase().formatHex(digest);
    }

    private static String newPassword() {
        byte[] random = new byte[24];
        new SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private static void restrict(Path file) throws IOException {
        if (!isWindows()) {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").startsWith("Windows");
    }
}
