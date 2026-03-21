/* =============================================
   ExamFlow — Admin Panel JavaScript
   File: js/admin-script.js
   ============================================= */

const BASE = "http://localhost:8085/Online_Examination_System";

let questionCount = 0;
let studentCount  = 0;
let createdExamId = null;  // stores examId after /add-exam

// ── On page load ───────────────────────────────────────────────────────
window.addEventListener("DOMContentLoaded", () => {
  const name = sessionStorage.getItem("adminName");
  if (name) document.getElementById("navAdminName").textContent = "👤 " + name;
});

// ── Toast ──────────────────────────────────────────────────────────────
function showToast(msg, type = "info") {
  const toast = document.getElementById("toast");
  toast.textContent = msg;
  toast.className   = "show " + type;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => { toast.className = ""; }, 3500);
}

// ── Overlay ────────────────────────────────────────────────────────────
function showOverlay(msg) {
  document.getElementById("publishingMsg").textContent = msg;
  document.getElementById("publishingOverlay").classList.add("show");
}
function hideOverlay() {
  document.getElementById("publishingOverlay").classList.remove("show");
}

// ── Logout ─────────────────────────────────────────────────────────────
function handleLogout() {
  sessionStorage.clear();
  fetch(`${BASE}/logout`, { method: "GET", credentials: "include" })
    .catch(() => {})
    .finally(() => { window.location.href = "/"; });
}

// ── Validate Step 1 ────────────────────────────────────────────────────
function validateStep1() {
  const title    = document.getElementById("examTitle").value.trim();
  const duration = document.getElementById("duration").value.trim();
  const marks    = document.getElementById("totalMarks").value.trim();
  if (!title)                        { showToast("⚠️ Please enter Exam Title.",    "error"); return; }
  if (!duration || parseInt(duration) < 1) { showToast("⚠️ Please enter Duration.",      "error"); return; }
  if (!marks    || parseInt(marks)    < 1) { showToast("⚠️ Please enter Total Marks.",   "error"); return; }
  goToStep(2);
}

// ── Step Navigation ────────────────────────────────────────────────────
function goToStep(n) {
  document.querySelectorAll(".panel").forEach(p => p.classList.remove("active"));
  document.getElementById("panel-" + n).classList.add("active");
  document.querySelectorAll(".step").forEach((s, i) => {
    s.classList.remove("active", "done");
    if (i + 1 < n)  s.classList.add("done");
    if (i + 1 === n) s.classList.add("active");
  });
  window.scrollTo({ top: 0, behavior: "smooth" });
}

// ── Sync counts ────────────────────────────────────────────────────────
function syncQuestionCount(val) {
  const n = parseInt(val) || 0;
  const list = document.getElementById("questionList");
  const current = list.querySelectorAll(".question-card").length;
  if (n > current) for (let i = current; i < n; i++) addQuestionCard();
  else if (n < current) {
    const cards = list.querySelectorAll(".question-card");
    for (let i = n; i < current; i++) cards[i].remove();
    updateQuestionChip();
  }
}

function syncStudentCount(val) {
  const n = parseInt(val) || 0;
  const list = document.getElementById("studentList");
  const current = list.querySelectorAll(".student-row").length;
  if (n > current) for (let i = current; i < n; i++) addStudentRow();
  else if (n < current) {
    const rows = list.querySelectorAll(".student-row");
    for (let i = n; i < current; i++) rows[i].remove();
    updateStudentChip();
  }
}

// ── Add Student Row ────────────────────────────────────────────────────
function addStudentRow() {
  studentCount++;
  const list = document.getElementById("studentList");
  const row  = document.createElement("div");
  row.className = "student-row";
  row.innerHTML = `
    <div class="student-index">Student #${studentCount}</div>
    <div class="field" style="grid-column:1">
      <input type="text" placeholder="Full Name" style="background:var(--bg)"/>
    </div>
    <div class="field" style="grid-column:2">
      <input type="text" placeholder="Roll / ID Number" style="background:var(--bg)"/>
    </div>
    <button class="btn btn-danger"
      onclick="removeRow(this,'student-row','studentCountChip','student')">✕</button>
  `;
  list.appendChild(row);
  updateStudentChip();
}

// ── Add Question Card ──────────────────────────────────────────────────
function addQuestionCard() {
  questionCount++;
  const n    = questionCount;
  const list = document.getElementById("questionList");
  const card = document.createElement("div");
  card.className = "question-card";
  card.id        = "qcard-" + n;
  card.innerHTML = `
    <div class="q-header">
      <span class="q-badge">Q ${n}</span>
      <button class="btn btn-danger"
        onclick="removeRow(this,'question-card','questionCountChip','question')">Remove</button>
    </div>
    <div class="field" style="margin-bottom:12px">
      <label>Question Text</label>
      <textarea placeholder="Type your question here..." class="q-text"></textarea>
    </div>
    <div class="options-grid">
      <div class="option-field">
        <span class="option-label-pill">A</span>
        <input type="text" placeholder="Option A" class="opt-a"/>
      </div>
      <div class="option-field">
        <span class="option-label-pill">B</span>
        <input type="text" placeholder="Option B" class="opt-b"/>
      </div>
      <div class="option-field">
        <span class="option-label-pill">C</span>
        <input type="text" placeholder="Option C" class="opt-c"/>
      </div>
      <div class="option-field">
        <span class="option-label-pill">D</span>
        <input type="text" placeholder="Option D" class="opt-d"/>
      </div>
    </div>
    <div class="correct-row">
      <label>Correct Answer:</label>
      <select class="correct-select">
        <option value="">— Select —</option>
        <option value="A">A</option>
        <option value="B">B</option>
        <option value="C">C</option>
        <option value="D">D</option>
      </select>
      <div class="field" style="flex:1;max-width:100px">
        <input type="number" placeholder="Marks" min="1" value="1"
               class="q-marks" style="background:var(--input-bg)"/>
      </div>
      <label style="margin:0;font-size:11px">marks</label>
    </div>
  `;
  list.appendChild(card);
  updateQuestionChip();
}

// ── Remove row ─────────────────────────────────────────────────────────
function removeRow(btn, cls, chipId, type) {
  btn.closest("." + cls).remove();
  if (type === "student") updateStudentChip();
  else updateQuestionChip();
}

// ── Chip counters ──────────────────────────────────────────────────────
function updateStudentChip() {
  const n = document.querySelectorAll(".student-row").length;
  document.getElementById("studentCountChip").textContent =
    n + " student" + (n !== 1 ? "s" : "");
  document.getElementById("numStudents").value = n;
}

function updateQuestionChip() {
  const n = document.querySelectorAll(".question-card").length;
  document.getElementById("questionCountChip").textContent =
    n + " question" + (n !== 1 ? "s" : "");
  document.getElementById("numQuestions").value = n;
}

// ── Build Review ───────────────────────────────────────────────────────
function buildReview() {
  if (!document.getElementById("examTitle").value.trim()) {
    showToast("⚠️ Please enter an exam title first.", "error"); return;
  }

  const title    = document.getElementById("examTitle").value    || "—";
  const subject  = document.getElementById("examSubject").value  || "—";
  const marks    = document.getElementById("totalMarks").value   || "—";
  const duration = document.getElementById("duration").value     || "—";
  const dlDate   = document.getElementById("deadlineDate").value || "—";
  const dlTime   = document.getElementById("deadlineTime").value || "—";

  document.getElementById("reviewGrid").innerHTML = `
    <div class="review-item"><div class="r-label">Title</div><div class="r-val">${title}</div></div>
    <div class="review-item"><div class="r-label">Subject</div><div class="r-val">${subject}</div></div>
    <div class="review-item"><div class="r-label">Total Marks</div><div class="r-val">${marks}</div></div>
    <div class="review-item"><div class="r-label">Duration</div><div class="r-val">${duration} min</div></div>
    <div class="review-item"><div class="r-label">Deadline Date</div><div class="r-val">${dlDate}</div></div>
    <div class="review-item"><div class="r-label">Deadline Time</div><div class="r-val">${dlTime}</div></div>
  `;

  const cards = document.querySelectorAll(".question-card");
  document.getElementById("reviewQCount").textContent = cards.length;
  let qHTML = "";
  cards.forEach((card, i) => {
    const qText   = card.querySelector(".q-text").value           || "(empty)";
    const correct = card.querySelector(".correct-select").value   || "?";
    const mks     = card.querySelector(".q-marks").value          || "1";
    qHTML += `<li><strong>Q${i+1}.</strong> ${qText} &nbsp;
      <span class="ans">✓ ${correct}</span>
      <span style="color:var(--muted);font-size:11px">[${mks} mark${mks>1?"s":""}]</span></li>`;
  });
  document.getElementById("reviewQuestions").innerHTML =
    qHTML || '<li style="color:var(--muted)">No questions added.</li>';

  const rows = document.querySelectorAll(".student-row");
  document.getElementById("reviewSCount").textContent = rows.length;
  let sHTML = "";
  rows.forEach((row, i) => {
    const inputs = row.querySelectorAll("input");
    const name   = inputs[0].value || "(no name)";
    const roll   = inputs[1].value || "(no ID)";
    sHTML += `<li><strong>${i+1}.</strong> ${name} <span style="color:var(--muted)">— ${roll}</span></li>`;
  });
  document.getElementById("reviewStudents").innerHTML =
    sHTML || '<li style="color:var(--muted)">No students added.</li>';

  goToStep(4);
}

// ── Publish Exam ───────────────────────────────────────────────────────
async function publishExam() {

  const title    = document.getElementById("examTitle").value.trim();
  const duration = parseInt(document.getElementById("duration").value) || 0;
  const marks    = parseInt(document.getElementById("totalMarks").value) || 0;
  const dlDate   = document.getElementById("deadlineDate").value;
  const dlTime   = document.getElementById("deadlineTime").value;

  if (!title)       { showToast("⚠️ Exam title is required.", "error");   return; }
  if (duration < 1) { showToast("⚠️ Please set a valid duration.", "error"); return; }
  if (marks    < 1) { showToast("⚠️ Please set total marks.", "error");   return; }

  // Build deadline string "yyyy-MM-dd HH:mm:ss"
  let deadline = null;
  if (dlDate && dlTime) {
    deadline = dlDate + " " + dlTime + ":00";
  } else if (dlDate) {
    deadline = dlDate + " 23:59:59";
  }

  // Collect questions
  const cards = document.querySelectorAll(".question-card");
  if (cards.length === 0) { showToast("⚠️ Add at least one question.", "error"); return; }

  const questions = [];
  let hasError = false;
  cards.forEach((card, i) => {
    const qText   = card.querySelector(".q-text").value.trim();
    const optA    = card.querySelector(".opt-a").value.trim();
    const optB    = card.querySelector(".opt-b").value.trim();
    const optC    = card.querySelector(".opt-c").value.trim();
    const optD    = card.querySelector(".opt-d").value.trim();
    const correct = card.querySelector(".correct-select").value;
    const qMarks  = parseInt(card.querySelector(".q-marks").value) || 1;
    if (!qText || !optA || !optB || !optC || !optD || !correct) {
      showToast(`⚠️ Question ${i+1} is incomplete.`, "error");
      hasError = true; return;
    }
    questions.push({ questionText: qText, optionA: optA, optionB: optB,
                     optionC: optC, optionD: optD, correctOption: correct, marks: qMarks });
  });
  if (hasError) return;

  // Collect students
  const studentRows = document.querySelectorAll(".student-row");
  const students = [];
  studentRows.forEach(row => {
    const inputs = row.querySelectorAll("input");
    const roll   = inputs[1] ? inputs[1].value.trim() : "";
    if (roll) students.push({ rollNo: roll });
  });

  // ── STEP 1: Create exam ──────────────────────────────────────────────
  showOverlay("Creating exam…");
  let examId;
  try {
    const params = new URLSearchParams();
    params.append("title",           title);
    params.append("durationMinutes", duration);
    params.append("totalMarks",      marks);

    const res  = await fetch(`${BASE}/add-exam`, {
      method: "POST", credentials: "include",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString(),
    });
    const data = await res.json();
    if (!data.success) { hideOverlay(); showToast("❌ " + data.message, "error"); return; }
    examId = data.examId;
    createdExamId = examId;
  } catch (err) {
    hideOverlay(); showToast("❌ Could not reach server. Is Tomcat running?", "error"); return;
  }

  // ── STEP 2: Add questions ────────────────────────────────────────────
  for (let i = 0; i < questions.length; i++) {
    showOverlay(`Adding question ${i+1} of ${questions.length}…`);
    const q = questions[i];
    try {
      const params = new URLSearchParams();
      params.append("examId",        examId);
      params.append("questionText",  q.questionText);
      params.append("optionA",       q.optionA);
      params.append("optionB",       q.optionB);
      params.append("optionC",       q.optionC);
      params.append("optionD",       q.optionD);
      params.append("correctOption", q.correctOption);
      params.append("marks",         q.marks);

      const res  = await fetch(`${BASE}/add-question`, {
        method: "POST", credentials: "include",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params.toString(),
      });
      const data = await res.json();
      if (!data.success) { hideOverlay(); showToast(`❌ Q${i+1}: ${data.message}`, "error"); return; }
    } catch (err) {
      hideOverlay(); showToast(`❌ Failed to save question ${i+1}.`, "error"); return;
    }
  }

  // ── STEP 3: Publish + enroll students ───────────────────────────────
  showOverlay("Publishing exam and enrolling students…");
  try {
    const res  = await fetch(`${BASE}/publish-exam`, {
      method: "POST", credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ examId, deadline, students }),
    });
    const data = await res.json();
    if (!data.success) { hideOverlay(); showToast("❌ " + data.message, "error"); return; }

    hideOverlay();
    document.getElementById("successMsg").textContent =
      `"${title}" (ID: ${examId}) published! ` +
      (data.enrolled > 0 ? `${data.enrolled} student(s) enrolled.` : "No students enrolled.") +
      (data.notFound  > 0 ? ` ${data.notFound} roll number(s) not found.` : "");
    goToStep(5);
    showToast("✅ Exam published successfully!", "success");

  } catch (err) {
    hideOverlay(); showToast("❌ Failed to publish exam.", "error");
  }
}

// ── Reset ──────────────────────────────────────────────────────────────
function resetAll() {
  document.getElementById("examTitle").value    = "";
  document.getElementById("examSubject").value  = "";
  document.getElementById("totalMarks").value   = "";
  document.getElementById("duration").value     = "";
  document.getElementById("deadlineDate").value = "";
  document.getElementById("deadlineTime").value = "";
  document.getElementById("numQuestions").value = "";
  document.getElementById("numStudents").value  = "";
  document.getElementById("studentList").innerHTML  = "";
  document.getElementById("questionList").innerHTML = "";
  questionCount = 0; studentCount = 0; createdExamId = null;
  updateStudentChip();
  updateQuestionChip();
  goToStep(1);
}