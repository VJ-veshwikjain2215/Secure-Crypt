"use strict";
const $ = (id) => document.getElementById(id);
let mode = "encrypt";
let busy = false;
let lastResult = null;
let sessionCount = 0;
const hints = {
  AES: "AES with a 128-bit key, CBC mode, and password-based key derivation. This implementation does not authenticate ciphertext.",
  CAESAR:
    "A fixed shift through the Latin alphabet. Enter a whole number, such as 3. For learning only; other alphabets have known limitations.",
  VIGENERE:
    "A repeating Latin-letter keyword, such as LEMON. For learning only; other alphabets have known limitations.",
  XOR: "Repeating-key XOR on UTF-8 bytes. Output is Base64 encoded. An educational transformation, not secure secret storage.",
  DES: "A historical cipher with a 56-bit effective key. Included for comparison, not for protecting new secrets.",
  TRIPLE_DES:
    "A legacy cipher applying DES three times. Included for comparison, not recommended for new secret protection.",
};
function status(message, error = false) {
  $("operationStatus").textContent = message;
  $("operationStatus").classList.toggle("error", error);
}
function updateCounters() {
  $("inputCount").textContent =
    `${$("input").value.length.toLocaleString()} / 20,000 characters`;
}
function setMode(next) {
  if (busy) return;
  mode = next;
  for (const name of ["encrypt", "decrypt"]) {
    const active = name === mode;
    const tab = $(name + "Mode");
    tab.classList.toggle("active", active);
    tab.setAttribute("aria-selected", String(active));
    tab.tabIndex = active ? 0 : -1;
  }
  $("operationPanel").setAttribute("aria-labelledby", mode + "Mode");
  $("inputLabel").textContent =
    mode === "encrypt" ? "YOUR MESSAGE" : "ENCRYPTED TEXT";
  $("outputLabel").textContent =
    mode === "encrypt" ? "ENCRYPTED RESULT" : "DECRYPTED MESSAGE";
  $("runLabel").textContent =
    mode === "encrypt" ? "Encrypt message" : "Decrypt message";
  $("input").placeholder =
    mode === "encrypt"
      ? "A message worth keeping to yourself…"
      : "Paste the encrypted text here…";
  $("outputFormat").textContent =
    mode === "decrypt" || ["CAESAR", "VIGENERE"].includes($("algorithm").value)
      ? "TEXT"
      : "BASE64";
  $("output").value = "";
  $("outputCount").textContent = "Waiting for input";
  $("emptySymbol").hidden = false;
  for (const id of ["copyOutput", "downloadOutput", "useOutput"])
    $(id).disabled = true;
  status("Ready when you are.");
}
function configureAlgorithm() {
  const algo = $("algorithm").value;
  $("algorithmHint").textContent = hints[algo];
  $("keyLabel").textContent =
    algo === "CAESAR"
      ? "SHIFT VALUE"
      : algo === "VIGENERE"
        ? "KEYWORD"
        : algo === "XOR"
          ? "REPEATING KEY"
          : "PASSPHRASE";
  $("key").placeholder =
    algo === "CAESAR"
      ? "A whole number, e.g. 3"
      : algo === "VIGENERE"
        ? "A Latin-letter keyword, e.g. LEMON"
        : "Use a unique sample passphrase";
  $("generateKey").disabled = busy || ["CAESAR", "VIGENERE"].includes(algo);
  setMode(mode);
}
function setBusy(value) {
  busy = value;
  $("runButton").disabled = value;
  $("runLabel").textContent = value
    ? "Working…"
    : mode === "encrypt"
      ? "Encrypt message"
      : "Decrypt message";
  for (const id of [
    "encryptMode",
    "decryptMode",
    "algorithm",
    "key",
    "input",
    "loadExample",
    "clearInput",
    "revealKey",
  ])
    $(id).disabled = value;
  $("generateKey").disabled =
    value || ["CAESAR", "VIGENERE"].includes($("algorithm").value);
  $("useOutput").disabled = value || !lastResult || !$("output").value;
  $("operationPanel").setAttribute("aria-busy", String(value));
}
async function run(event) {
  event?.preventDefault();
  if (busy) return;
  const text = $("input").value,
    key = $("key").value,
    algorithm = $("algorithm").value;
  if (!text.trim()) {
    status("Add some text to get started.", true);
    $("input").focus();
    return;
  }
  if (text.length > 20000) {
    status(
      "The current API accepts up to 20,000 input characters. This result exceeds that limit.",
      true,
    );
    return;
  }
  if (!key.trim()) {
    status("Add a sample key or passphrase.", true);
    $("key").focus();
    return;
  }
  if (algorithm === "CAESAR" && !/^[+-]?\d+$/.test(key.trim())) {
    status("Caesar needs a whole number, such as 3.", true);
    return;
  }
  if (algorithm === "VIGENERE" && !/[a-z]/i.test(key)) {
    status("Vigenère needs a Latin-letter keyword.", true);
    return;
  }
  setBusy(true);
  status("Running the operation…");
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 30000);
  try {
    const response = await fetch(`/api/${mode}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ text, algorithm, key }),
      signal: controller.signal,
      cache: "no-store",
    });
    const data = await response.json();
    if (!response.ok || !data.success)
      throw new Error(data.message || "The operation could not be completed.");
    $("output").value = data.result;
    $("emptySymbol").hidden = true;
    $("outputCount").textContent =
      `${data.result.length.toLocaleString()} characters`;
    lastResult = { text: data.result, algorithm, operation: mode };
    $("copyOutput").disabled = false;
    $("downloadOutput").disabled = false;
    status(
      mode === "encrypt"
        ? "Encrypted. You will need the same key to decrypt this result."
        : "Decrypted. Your message is ready.",
    );
    addHistory(mode, algorithm, text.length);
  } catch (error) {
    status(
      error.name === "AbortError"
        ? "That took too long. Try again with a smaller sample."
        : error.message === "Failed to fetch"
          ? "Cannot reach the server. Check your connection and try again."
          : error.message,
      true,
    );
  } finally {
    clearTimeout(timeout);
    setBusy(false);
  }
}
function addHistory(operation, algorithm, length) {
  sessionCount++;
  $("historyCount").textContent = String(sessionCount).padStart(2, "0");
  $("historyEmpty").hidden = true;
  $("historyTable").hidden = false;
  const row = document.createElement("tr");
  for (const value of [
    operation === "encrypt" ? "↗ Encrypt" : "↙ Decrypt",
    algorithm,
    length.toLocaleString() + " chars",
    new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
  ]) {
    const cell = document.createElement("td");
    cell.textContent = value;
    row.append(cell);
  }
  $("historyBody").prepend(row);
  while ($("historyBody").children.length > 20)
    $("historyBody").lastElementChild.remove();
}
$("operationPanel").addEventListener("submit", run);
for (const name of ["encrypt", "decrypt"]) {
  $(name + "Mode").addEventListener("click", () => setMode(name));
  $(name + "Mode").addEventListener("keydown", (event) => {
    if (["ArrowLeft", "ArrowRight", "Home", "End"].includes(event.key)) {
      event.preventDefault();
      const next =
        event.key === "Home"
          ? "encrypt"
          : event.key === "End"
            ? "decrypt"
            : mode === "encrypt"
              ? "decrypt"
              : "encrypt";
      setMode(next);
      $(next + "Mode").focus();
    }
  });
}
$("algorithm").addEventListener("change", configureAlgorithm);
$("input").addEventListener("input", updateCounters);
$("revealKey").addEventListener("click", () => {
  const reveal = $("key").type === "password";
  $("key").type = reveal ? "text" : "password";
  $("revealKey").textContent = reveal ? "Hide" : "Show";
  $("revealKey").setAttribute(
    "aria-label",
    reveal ? "Hide passphrase" : "Show passphrase",
  );
  $("revealKey").setAttribute("aria-pressed", String(reveal));
});
$("generateKey").addEventListener("click", () => {
  const bytes = crypto.getRandomValues(new Uint8Array(18));
  $("key").value = Array.from(bytes, (b) =>
    b.toString(16).padStart(2, "0"),
  ).join("");
  status(
    "Random sample key generated locally. It has not been sent to the server.",
  );
});
$("clearInput").addEventListener("click", () => {
  $("input").value = "";
  $("key").value = "";
  lastResult = null;
  setMode(mode);
  updateCounters();
  $("input").focus();
});
$("loadExample").addEventListener("click", () => {
  setMode("encrypt");
  const algorithm = $("algorithm").value;
  $("input").value =
    algorithm === "VIGENERE"
      ? "ATTACKATDAWN"
      : "Meet me at the library at noon.";
  $("key").value =
    algorithm === "CAESAR"
      ? "3"
      : algorithm === "VIGENERE"
        ? "LEMON"
        : "A-sample-passphrase-729";
  updateCounters();
  status("Example loaded. This is sample data, not a real secret.");
});
$("useOutput").addEventListener("click", () => {
  if (!lastResult || busy) return;
  const previous = lastResult;
  setMode(previous.operation === "encrypt" ? "decrypt" : "encrypt");
  $("algorithm").value = previous.algorithm;
  $("input").value = previous.text;
  updateCounters();
  status("Result transferred. Use the same key to reverse the operation.");
  $("input").focus();
});
$("copyOutput").addEventListener("click", async () => {
  try {
    await navigator.clipboard.writeText($("output").value);
    status("Result copied to your clipboard.");
  } catch {
    status("Copy was blocked. Select and copy the result manually.", true);
  }
});
$("downloadOutput").addEventListener("click", () => {
  const blob = new Blob([$("output").value], {
    type: "text/plain;charset=utf-8",
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `securecrypt-${mode === "encrypt" ? "encrypted" : "decrypted"}.txt`;
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  status("Result saved as a text file.");
});
$("clearHistory").addEventListener("click", () => {
  $("historyBody").replaceChildren();
  sessionCount = 0;
  $("historyCount").textContent = "00";
  $("historyTable").hidden = true;
  $("historyEmpty").hidden = false;
  status("Session history cleared.");
});
document.addEventListener("keydown", (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key === "Enter") {
    event.preventDefault();
    run();
  }
});
function applyTheme(theme) {
  document.documentElement.dataset.theme = theme;
  $("themeToggle").setAttribute(
    "aria-label",
    `Switch to ${theme === "light" ? "dark" : "light"} theme`,
  );
}
try {
  applyTheme(sessionStorage.getItem("sc-workbench-theme") || "light");
} catch {
  applyTheme("light");
}
$("themeToggle").addEventListener("click", () => {
  const next =
    document.documentElement.dataset.theme === "light" ? "dark" : "light";
  applyTheme(next);
  try {
    sessionStorage.setItem("sc-workbench-theme", next);
  } catch {
    /* Theme works without storage. */
  }
});
configureAlgorithm();
