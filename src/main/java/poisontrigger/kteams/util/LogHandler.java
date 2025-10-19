package poisontrigger.kteams.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class LogHandler {

    private static final Logger LOGGER = LogManager.getLogger("kteams-logs");

    // --- singleton ---
    private static final LogHandler INSTANCE = new LogHandler();
    public static LogHandler get() { return INSTANCE; }

    private final ConcurrentLinkedQueue<String> buffer = new ConcurrentLinkedQueue<>();
    private final AtomicLong nextId; // global, monotonic

    private File outDir = null;
    private File outFile = null;

    private LogHandler() {
        // start at 1; may bump this after we know the output file path
        this.nextId = new AtomicLong(1);
    }
    public synchronized void init(File worldDir) {
        if (outFile != null) return; // already initialized
        outDir = new File(worldDir, "kTeams-logs");
        // make a daily file or single file — here: single file
        outFile = new File(outDir, "kteams_logs.csv");
        if (!outDir.exists() && !outDir.mkdirs()) {
            LOGGER.warn("Couldn't create log folder: {}", outDir.getAbsolutePath());
        }
        // bump nextId to (lastId+1) from existing file if present
        long last = readLastId(outFile);
        if (last >= 1) nextId.set(last + 1);
        // ensure header exists
        ensureHeader(outFile);
        LOGGER.info("LogHandler initialized -> {}", outFile.getAbsolutePath());
    }

    private static long readLastId(File file) {
        if (file == null || !file.exists() || file.length() == 0L) return 0;
        final int TAIL = 64 * 1024;
        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
            long len = raf.length();
            long start = Math.max(0, len - TAIL);
            raf.seek(start);
            byte[] buf = new byte[(int)(len - start)];
            raf.readFully(buf);
            String tail = new String(buf, java.nio.charset.StandardCharsets.UTF_8);
            String[] lines = tail.split("\\R");
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("timeMillis,")) continue;
                String[] parts = line.split(",", 3); // id is 2nd column
                if (parts.length >= 2) {
                    try { return Long.parseLong(parts[1].trim()); }
                    catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ignored) {}
        return 0;
    }

    private static void ensureHeader(File file) {
        if (file == null) return;
        if (!file.exists() || file.length() == 0L) {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file, true);
                 java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(osw)) {
                bw.write("timeMillis,id,message,playerUuid,playerName,flagCategory");
                bw.newLine();
            } catch (IOException ignored) {}
        }
    }

    public long nextId() { return nextId.getAndIncrement(); }

    // -------- Data --------
    private final List<LogEntry> logs = Collections.synchronizedList(new ArrayList<>());

    public static final class LogEntry {
        public final String id;
        public final long timeMillis;
        public final String message;
        @Nullable
        public final UUID playerUuid;       // null if not applicable
        @Nullable public final String playerNameHint; // optional cached name for readability
        public final String flagCategory;             // e.g. "FLAG.CAPTURE", "FLAG.PLACED"

        private LogEntry(String id, long t, String msg, @Nullable UUID uuid, @Nullable String name, String cat) {
            this.id = id;
            this.timeMillis = t;
            this.message = msg;
            this.playerUuid = uuid;
            this.playerNameHint = name;
            this.flagCategory = cat;
        }

        public String formatLine() {
            String ts = new SimpleDateFormat("HH:mm:ss").format(new Date(timeMillis));
            String playerPart = (playerUuid != null)
                    ? (playerNameHint != null ? playerNameHint : playerUuid.toString())
                    : "-";
            return "(" + ts + ") " +
                    "(#" + id + ") " +
                    "(" + message + ") " +
                    "(" + playerPart + ") " +
                    "(" + flagCategory + ")";
        }

        // CSV-safe (very simple escaping of quotes)
        public String toCsv() {
            return String.join(",",
                    Long.toString(timeMillis),
                    id,
                    csvEscape(message),
                    playerUuid == null ? "" : playerUuid.toString(),
                    csvEscape(playerNameHint == null ? "" : playerNameHint),
                    csvEscape(flagCategory)
            );
        }

        private static String csvEscape(String s) {
            String q = s.replace("\"", "\"\"");
            return "\"" + q + "\"";
        }
    }

    // -------- Create logs --------
    public LogEntry log(String message, @Nullable UUID playerUuid, @Nullable String playerNameHint, String flagCategory) {
        if (message == null) message = "";
        if (flagCategory == null) flagCategory = "FLAG.UNKNOWN";
        // Slightly Serialized, Not 100% Collision Proof
        String id = Base62.hexToBase62(Long.toHexString(nextId()) + Long.toHexString(System.currentTimeMillis()));
        LogEntry e = new LogEntry(id, System.currentTimeMillis(), message, playerUuid, playerNameHint, flagCategory);
        logs.add(e);
        return e;
    }
    public void flushLogs(){
        logs.clear();
    }

    // Convenience overloads
    public LogEntry log(String message, String flagCategory) {
        return log(message, null, null, flagCategory);
    }
    public LogEntry logPlayer(String message, EntityPlayerMP player, String flagCategory) {
        return log(message, player.getUniqueID(), player.getName(), flagCategory);
    }

    // -------- Queries --------
    public @Nullable LogEntry getById(String id) {
        synchronized (logs) {
            for (LogEntry e : logs) if (e.id == id) return e;
        }
        return null;
    }

    public List<LogEntry> getByPlayer(UUID playerUuid) {
        synchronized (logs) {
            return logs.stream()
                    .filter(e -> playerUuid.equals(e.playerUuid))
                    .collect(Collectors.toList());
        }
    }

    public List<LogEntry> getByCategory(String categoryExact) {
        synchronized (logs) {
            return logs.stream()
                    .filter(e -> e.flagCategory.equalsIgnoreCase(categoryExact))
                    .collect(Collectors.toList());
        }
    }


    public List<LogEntry> getByCategoryPrefix(String categoryPrefix) {
        String pref = categoryPrefix.toLowerCase(Locale.ROOT);
        synchronized (logs) {
            return logs.stream()
                    .filter(e -> e.flagCategory.toLowerCase(Locale.ROOT).startsWith(pref))
                    .collect(Collectors.toList());
        }
    }

    public List<LogEntry> getByTimeRange(long startMillis, long endMillis) {
        synchronized (logs) {
            return logs.stream()
                    .filter(e -> e.timeMillis >= startMillis && e.timeMillis <= endMillis)
                    .collect(Collectors.toList());
        }
    }

    // Most recent N logs.
    public List<LogEntry> getLatest(int limit) {
        synchronized (logs) {
            int n = logs.size();
            if (limit <= 0) return Collections.emptyList();
            int from = Math.max(0, n - limit);
            return new ArrayList<>(logs.subList(from, n));
        }
    }

    // -------- Save to file (CSV) --------

    public void saveToFile(File directory, String filename, boolean append) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory: " + directory);
        }
        File out = new File(directory, filename);
        boolean writeHeader = !append || !out.exists();

        try (FileWriter fw = new FileWriter(out, append);
             BufferedWriter bw = new BufferedWriter(fw)) {
            if (writeHeader) {
                bw.write("timeMillis,id,message,playerUuid,playerName,flagCategory");
                bw.newLine();
            }
            synchronized (logs) {
                for (LogEntry e : logs) {
                    bw.write(e.toCsv());
                    bw.newLine();
                }
            }
        }
    }

    // -------- Send to player (paginated) --------
    public void sendToPlayer(EntityPlayerMP target, List<LogEntry> entries, int page, int pageSize) {
        if (target == null || entries == null || entries.isEmpty()) {
            sendLine(target, TextFormatting.GRAY + "(no logs)");
            return;
        }
        pageSize = Math.max(1, pageSize);
        int pages = (entries.size() + pageSize - 1) / pageSize;
        page = Math.max(1, Math.min(page, pages));
        int from = (page - 1) * pageSize;
        int to = Math.min(entries.size(), from + pageSize);

        sendLine(target, TextFormatting.YELLOW + "---- Logs (" + page + "/" + pages + ") ----");

        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        for (int i = from; i < to; i++) {
            LogEntry e = entries.get(i);
            String ts = fmt.format(new Date(e.timeMillis));
            String id = "#" + e.id;
            String playerPart = (e.playerUuid != null)
                    ? (e.playerNameHint != null ? e.playerNameHint : e.playerUuid.toString())
                    : "-";

            String line = TextFormatting.DARK_GRAY + "(" + ts + ") " +
                    TextFormatting.GRAY + "(" + id + ") " +
                    TextFormatting.WHITE + "(" + e.message + ") " +
                    TextFormatting.AQUA + "(" + playerPart + ") " +
                    TextFormatting.GOLD + "(" + e.flagCategory + ")";

            target.sendMessage(new TextComponentString(line));
        }
    }

    private static void sendLine(@Nullable EntityPlayerMP p, String msg) {
        if (p != null) p.sendMessage(new TextComponentString(msg));
    }

    // -------- Optional helpers --------


    public List<LogEntry> getLastMinutes(int minutes) {
        long now = System.currentTimeMillis();
        long from = now - Math.max(1, minutes) * 60_000L;
        return getByTimeRange(from, now);
    }


    public static File suggestedSaveDir(MinecraftServer server) {

        // This picks the root saves folder and writes into "kTeams".
        File root = server.getFile("."/*run dir*/);
        File kdir = new File(root, "kTeams-logs");
        return kdir;
    }
}