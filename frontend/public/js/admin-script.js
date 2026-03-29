// @ts-nocheck
/* =============================================
   ExamFlow — Admin Panel JavaScript
   College-scoped: admin only sees/manages
   students from their own college.
   ============================================= */

const BASE = "http://localhost:8085/Online_Examination_System";

let questionCount         = 0;
let studentCount          = 0;
let createdExamId         = null;
let allRegisteredStudents = [];
let allGroupedStudents    = {};
let selectedStudentIds    = new Set();

// ── College-aware headers ─────────────────────────────────────────────────────
function getCollegeName() {
  return sessionStorage.getItem("collegeName") || "";
}
function getAdminId() {
  return sessionStorage.getItem("adminId") || "";
}
function formHeaders() {
  var h = {
    "Content-Type": "application/x-www-form-urlencoded",
    "X-Admin-Id":   getAdminId()
  };
  if (getCollegeName()) h["X-College-Name"] = getCollegeName();
  return h;
}
function jsonHeaders() {
  var h = {
    "Content-Type": "application/json",
    "X-Admin-Id":   getAdminId()
  };
  if (getCollegeName()) h["X-College-Name"] = getCollegeName();
  return h;
}

// ── Init ───────────────────────────────────────────────────────────────────────
window.addEventListener("DOMContentLoaded", function () {
  // Show admin name
  var name    = sessionStorage.getItem("adminName")   || "Admin";
  var college = getCollegeName();
  var navName = document.getElementById("navAdminName");
  if (navName) navName.textContent = "👤 " + name;

  // Show college badge next to name
  if (college && navName) {
    var badge = document.createElement("span");
    badge.style.cssText =
      "background:var(--primary-light);color:var(--primary-dark);" +
      "border:1px solid #c7d2fe;padding:3px 12px;border-radius:999px;" +
      "font-size:11px;font-weight:700;font-family:var(--font-mono);";
    badge.textContent = "🏫 " + college;
    navName.after(badge);
  }
});

// ── Step Navigation ───────────────────────────────────────────────────────────
function goToStep(n) {
  document.querySelectorAll(".panel").forEach(function (p) {
    p.classList.remove("active");
  });
  document.getElementById("panel-" + n).classList.add("active");
  document.querySelectorAll(".step").forEach(function (s, i) {
    s.classList.remove("active", "done");
    if (i + 1 < n)  s.classList.add("done");
    if (i + 1 === n) s.classList.add("active");
  });
  window.scrollTo({ top: 0, behavior: "smooth" });

  // Load college students + batches when Step 2 opens
  if (n === 2) {
    if (allRegisteredStudents.length === 0) loadRegisteredStudents();
    loadBatchesForSelector();
  }
}

// ── Step 1 validation ─────────────────────────────────────────────────────────
function validateStep1() {
  var title    = document.getElementById("examTitle").value.trim();
  var duration = document.getElementById("duration").value.trim();
  var marks    = document.getElementById("totalMarks").value.trim();
  if (!title)    { showToast("⚠️ Please enter an exam title.", "error"); return; }
  if (!duration) { showToast("⚠️ Please enter exam duration.", "error"); return; }
  if (!marks)    { showToast("⚠️ Please enter total marks.",   "error"); return; }
  goToStep(2);
}

// ── Tab switcher (Step 2) ─────────────────────────────────────────────────────
function switchTab(tab) {
  var isSelect = tab === "select";
  document.getElementById("tabSelectPane").style.display = isSelect ? "block" : "none";
  document.getElementById("tabManualPane").style.display = isSelect ? "none"  : "block";
  document.getElementById("tabSelect").className = isSelect ? "btn btn-primary" : "btn btn-outline";
  document.getElementById("tabManual").className = isSelect ? "btn btn-outline" : "btn btn-primary";
}

// ── Load batches for the exam batch selector ────────────────────────────────
async function loadBatchesForSelector() {
  var select = document.getElementById("batchSelect");
  if (!select) return;
  try {
    var res  = await fetch(BASE + "/admin/data?action=batches", {
      credentials: "include",
      headers: { "X-Admin-Id": getAdminId(), "X-College-Name": getCollegeName() }
    });
    var data = await res.json();
    if (!data.success) return;
    var batches = data.batches || [];
    // Reset options
    select.innerHTML = "<option value=''>— No batch, add students manually below —</option>";
    batches.forEach(function(b) {
      var opt = document.createElement("option");
      opt.value = b.id;
      opt.textContent = b.name +
        " (" + b.standard + (b.division ? " Div-" + b.division : "") +
        (b.stream ? " · " + b.stream : "") + ")" +
        " — " + b.studentCount + " student" + (b.studentCount !== 1 ? "s" : "");
      select.appendChild(opt);
    });
  } catch (e) {
    console.error("loadBatchesForSelector error:", e);
  }
}

function onBatchSelect(batchId) {
  var info  = document.getElementById("batchInfo");
  var count = document.getElementById("batchStudentCount");
  if (batchId) {
    var opt = document.querySelector("#batchSelect option[value='" + batchId + "']");
    if (opt) {
      var match = opt.textContent.match(/(\d+) student/);
      if (match) count.textContent = "👥 " + match[1] + " students will be enrolled";
    }
    if (info) info.style.display = "block";
  } else {
    if (info)  info.style.display  = "none";
    if (count) count.textContent = "";
  }
}

// ── Load students filtered by admin's college ─────────────────────────────────
async function loadRegisteredStudents() {
  var loadingLabel = document.getElementById("loadingLabel");
  if (loadingLabel) loadingLabel.textContent = "Loading…";

  try {
    var res  = await fetch(BASE + "/admin/students", {
      credentials: "include",
      headers: { "X-College-Name": getCollegeName(), "X-Admin-Id": getAdminId() }
    });
    var data = await res.json();

    if (!data.success) {
      if (loadingLabel) loadingLabel.textContent = "Failed to load.";
      return;
    }

    allRegisteredStudents = data.students || [];
    allGroupedStudents    = data.grouped  || {};

    var college = data.collegeName || getCollegeName() || "Your College";
    var total   = allRegisteredStudents.length;

    if (loadingLabel) {
      loadingLabel.textContent = total + " student" +
        (total !== 1 ? "s" : "") + " from " + college;
    }

    renderGroupedStudents(allGroupedStudents);

  } catch (e) {
    console.error(e);
    if (loadingLabel) loadingLabel.textContent = "Cannot reach server.";
  }
}

// ── Render grouped by class/stream ────────────────────────────────────────────
function renderGroupedStudents(grouped) {
  var list = document.getElementById("registeredList");
  list.innerHTML = "";

  var keys = Object.keys(grouped).sort();

  if (keys.length === 0) {
    var empty = document.createElement("div");
    empty.style.cssText =
      "text-align:center;padding:32px;color:var(--muted-foreground);" +
      "font-size:13px;line-height:1.6;";
    empty.innerHTML =
      "<div style='font-size:2rem;margin-bottom:8px;'>👥</div>" +
      "<div style='font-weight:700;margin-bottom:4px;'>No students found</div>" +
      "<div>No students from " + (getCollegeName() || "your college") +
      " are registered yet.</div>";
    list.appendChild(empty);
    updateSelectedCount();
    return;
  }

  keys.forEach(function (groupKey) {
    var students = grouped[groupKey];

    // ── Group header ──────────────────────────────────────────────────────────
    var header = document.createElement("div");
    header.style.cssText =
      "display:flex;align-items:center;justify-content:space-between;" +
      "padding:8px 4px 6px;margin-top:10px;border-bottom:1px solid var(--border);";

    var left = document.createElement("div");
    left.style.cssText = "display:flex;align-items:center;gap:8px;";

    var dot = document.createElement("span");
    dot.style.cssText =
      "width:8px;height:8px;border-radius:50%;" +
      "background:var(--primary);display:inline-block;";
    left.appendChild(dot);

    var title = document.createElement("span");
    title.style.cssText =
      "font-family:var(--font-mono);font-size:11px;font-weight:600;" +
      "text-transform:uppercase;letter-spacing:0.5px;color:var(--foreground);";
    title.textContent = groupKey;
    left.appendChild(title);

    var countBadge = document.createElement("span");
    countBadge.style.cssText =
      "font-size:10px;font-family:var(--font-mono);" +
      "background:var(--primary-light);color:var(--primary-dark);" +
      "border:1px solid #c7d2fe;padding:1px 8px;border-radius:999px;";
    countBadge.textContent = students.length;
    left.appendChild(countBadge);
    header.appendChild(left);

    // Select all in group
    var selectAllBtn = document.createElement("button");
    selectAllBtn.type = "button";
    selectAllBtn.style.cssText =
      "font-size:11px;font-weight:600;color:var(--primary);" +
      "background:none;border:none;cursor:pointer;padding:2px 6px;";
    selectAllBtn.textContent = "Select All";
    (function (grpStudents) {
      selectAllBtn.onclick = function () {
        grpStudents.forEach(function (s) {
          selectedStudentIds.add(s.id);
          var chk = document.getElementById("chk-" + s.id);
          if (chk) chk.checked = true;
        });
        updateSelectedCount();
      };
    })(students);
    header.appendChild(selectAllBtn);
    list.appendChild(header);

    // ── Students in group ─────────────────────────────────────────────────────
    students.forEach(function (s) {
      list.appendChild(buildStudentLabel(s));
    });
  });

  updateSelectedCount();
}

// ── Build single student label ────────────────────────────────────────────────
function buildStudentLabel(s) {
  var isChecked = selectedStudentIds.has(s.id);

  var label = document.createElement("label");
  label.style.cssText =
    "display:flex;align-items:center;gap:12px;padding:10px 12px;" +
    "border:1.5px solid var(--border);border-radius:var(--radius);" +
    "cursor:pointer;margin-top:4px;transition:all .15s;" +
    "background:var(--background);";
  if (isChecked) {
    label.style.borderColor = "var(--primary)";
    label.style.background  = "var(--primary-light)";
  }

  (function (sid, rn, fn) {
    label.onclick = function () { toggleStudent(sid, rn, fn); };
  })(s.id, s.rollNo, s.fullName);

  // Checkbox
  var chk     = document.createElement("input");
  chk.type    = "checkbox";
  chk.id      = "chk-" + s.id;
  chk.checked = isChecked;
  chk.style.cssText =
    "width:16px;height:16px;accent-color:var(--primary);" +
    "flex-shrink:0;cursor:pointer;";
  (function (sid, rn, fn) {
    chk.onclick = function (e) {
      e.stopPropagation();
      toggleStudent(sid, rn, fn);
    };
  })(s.id, s.rollNo, s.fullName);
  label.appendChild(chk);

  // Avatar
  var avatar = document.createElement("div");
  avatar.style.cssText =
    "width:34px;height:34px;border-radius:50%;flex-shrink:0;" +
    "background:linear-gradient(135deg,var(--primary),var(--accent));" +
    "color:#fff;display:flex;align-items:center;justify-content:center;" +
    "font-family:var(--font-display);font-size:13px;font-weight:700;";
  avatar.textContent = s.fullName.charAt(0).toUpperCase();
  label.appendChild(avatar);

  // Info
  var info   = document.createElement("div");
  info.style.cssText = "flex:1;min-width:0;";
  var nameEl = document.createElement("div");
  nameEl.style.cssText =
    "font-family:var(--font-display);font-size:13px;font-weight:700;" +
    "white-space:nowrap;overflow:hidden;text-overflow:ellipsis;";
  nameEl.textContent = s.fullName;
  var metaEl = document.createElement("div");
  metaEl.style.cssText =
    "font-size:11px;color:var(--muted-foreground);margin-top:1px;";
  metaEl.textContent = s.rollNo + " · " + s.email;
  info.appendChild(nameEl);
  info.appendChild(metaEl);
  label.appendChild(info);

  // Class badge
  var badge = document.createElement("span");
  badge.style.cssText =
    "font-size:10px;font-family:var(--font-mono);flex-shrink:0;" +
    "background:var(--surface);border:1px solid var(--border);" +
    "padding:2px 8px;border-radius:999px;color:var(--muted-foreground);";
  badge.textContent = s.classLevel + (s.stream ? " · " + s.stream : "");
  label.appendChild(badge);

  return label;
}

// ── Render flat list (for search results) ─────────────────────────────────────
function renderRegisteredStudents(students) {
  var list = document.getElementById("registeredList");
  list.innerHTML = "";
  if (!students || students.length === 0) {
    var empty = document.createElement("div");
    empty.style.cssText =
      "text-align:center;padding:24px;color:var(--muted-foreground);font-size:13px;";
    empty.textContent = "No students match your search.";
    list.appendChild(empty);
    updateSelectedCount();
    return;
  }
  students.forEach(function (s) {
    list.appendChild(buildStudentLabel(s));
  });
  updateSelectedCount();
}

// ── Toggle student selection ──────────────────────────────────────────────────
function toggleStudent(id, rollNo, name) {
  var chk = document.getElementById("chk-" + id);
  if (selectedStudentIds.has(id)) {
    selectedStudentIds.delete(id);
    if (chk) {
      chk.checked = false;
      var lbl = chk.closest("label");
      if (lbl) {
        lbl.style.borderColor = "var(--border)";
        lbl.style.background  = "var(--background)";
      }
    }
  } else {
    selectedStudentIds.add(id);
    if (chk) {
      chk.checked = true;
      var lbl = chk.closest("label");
      if (lbl) {
        lbl.style.borderColor = "var(--primary)";
        lbl.style.background  = "var(--primary-light)";
      }
    }
  }
  updateSelectedCount();
}

// ── Toggle all visible ────────────────────────────────────────────────────────
function toggleSelectAll(checked) {
  document.querySelectorAll("#registeredList input[type=checkbox]")
    .forEach(function (chk) {
      var id = parseInt(chk.id.replace("chk-", ""));
      chk.checked = checked;
      if (checked) selectedStudentIds.add(id);
      else         selectedStudentIds.delete(id);
    });
  updateSelectedCount();
}

// ── Search — filters within college only ──────────────────────────────────────
var searchTimer = null;
function filterStudents(query) {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(function () {
    var q = query.toLowerCase().trim();
    if (!q) {
      renderGroupedStudents(allGroupedStudents);
    } else {
      var filtered = allRegisteredStudents.filter(function (s) {
        return s.fullName.toLowerCase().indexOf(q) >= 0 ||
               s.rollNo.toLowerCase().indexOf(q)   >= 0 ||
               s.email.toLowerCase().indexOf(q)    >= 0;
      });
      renderRegisteredStudents(filtered);
    }
    // Restore checked state
    selectedStudentIds.forEach(function (id) {
      var chk = document.getElementById("chk-" + id);
      if (chk) chk.checked = true;
    });
  }, 250);
}

// ── Update selected count chip ────────────────────────────────────────────────
function updateSelectedCount() {
  var manualCount = document.querySelectorAll(".student-email-input").length;
  var total = selectedStudentIds.size + manualCount;
  var chip  = document.getElementById("studentCountChip");
  if (chip) chip.textContent = total + " selected";
  var num = document.getElementById("numStudents");
  if (num) num.value = total;
}

function updateStudentChip() { updateSelectedCount(); }

// ── Get all selected students for publish ─────────────────────────────────────
function getSelectedStudents() {
  var students = [];
  // From checkbox (college students)
  selectedStudentIds.forEach(function (id) {
    var s = allRegisteredStudents.find(function (x) { return x.id === id; });
    if (s) students.push({ rollNo: s.rollNo });
  });
  // From manual email entries
  document.querySelectorAll(".student-email-input").forEach(function (input) {
    var email = input.value.trim();
    if (email) students.push({ email: email });
  });
  return students;
}

// ── Add manual email row ──────────────────────────────────────────────────────
function addEmailRow() {
  studentCount++;
  var list = document.getElementById("studentList");
  var row  = document.createElement("div");
  row.className = "student-row";
  row.innerHTML =
    "<div style='font-size:11px;font-family:var(--font-mono);" +
    "color:var(--primary);margin-bottom:6px;'>Entry #" + studentCount + "</div>" +
    "<div style='display:flex;gap:8px;align-items:center;'>" +
    "<input type='email' placeholder='student@email.com'" +
    " class='student-email-input'" +
    " style='flex:1;padding:8px 12px;border:1.5px solid var(--border);" +
    "border-radius:var(--radius);font-size:13px;outline:none;'" +
    " onfocus=\"this.style.borderColor='var(--primary)'\"" +
    " onblur=\"this.style.borderColor='var(--border)'\"/>" +
    "<button type='button' class='btn btn-danger'" +
    " onclick=\"removeRow(this,'student-row','studentCountChip','student')\">✕</button>" +
    "</div>";
  list.appendChild(row);
  updateStudentChip();
}

function addStudentRow() { addEmailRow(); }

function syncStudentCount(val) {
  var n       = parseInt(val) || 0;
  var list    = document.getElementById("studentList");
  var current = list.querySelectorAll(".student-row").length;
  if (n > current) {
    for (var i = current; i < n; i++) addEmailRow();
  } else if (n < current) {
    var rows = list.querySelectorAll(".student-row");
    for (var i = n; i < current; i++) rows[i].remove();
    updateStudentChip();
  }
}

// ── Question card ──────────────────────────────────────────────────────────────
function syncQuestionCount(val) {
  var n       = parseInt(val) || 0;
  var list    = document.getElementById("questionList");
  var current = list.querySelectorAll(".question-card").length;
  if (n > current) {
    for (var i = current; i < n; i++) addQuestionCard();
  } else if (n < current) {
    var cards = list.querySelectorAll(".question-card");
    for (var i = n; i < current; i++) cards[i].remove();
    updateQuestionChip();
  }
}

function addQuestionCard() {
  questionCount++;
  var n    = questionCount;
  var list = document.getElementById("questionList");
  var card = document.createElement("div");
  card.className = "question-card";
  card.id        = "qcard-" + n;
  card.innerHTML =
    "<div class='q-header'>" +
      "<span class='q-badge'>Q " + n + "</span>" +
      "<button class='btn btn-danger' onclick=\"removeRow(this,'question-card','questionCountChip','question')\">Remove</button>" +
    "</div>" +
    "<div class='field' style='margin-bottom:12px'>" +
      "<label>Question Text</label>" +
      "<textarea placeholder='Type your question here...' class='q-text'></textarea>" +
    "</div>" +
    "<div class='options-grid'>" +
      "<div class='option-field'><span class='option-label-pill'>A</span><input type='text' placeholder='Option A' class='opt-a'/></div>" +
      "<div class='option-field'><span class='option-label-pill'>B</span><input type='text' placeholder='Option B' class='opt-b'/></div>" +
      "<div class='option-field'><span class='option-label-pill'>C</span><input type='text' placeholder='Option C' class='opt-c'/></div>" +
      "<div class='option-field'><span class='option-label-pill'>D</span><input type='text' placeholder='Option D' class='opt-d'/></div>" +
    "</div>" +
    "<div class='correct-row'>" +
      "<label>Correct Answer:</label>" +
      "<select class='correct-select'>" +
        "<option value=''>— Select —</option>" +
        "<option value='A'>A</option>" +
        "<option value='B'>B</option>" +
        "<option value='C'>C</option>" +
        "<option value='D'>D</option>" +
      "</select>" +
      "<div class='field' style='flex:1;max-width:100px'>" +
        "<input type='number' placeholder='Marks' min='1' value='1' class='q-marks'/>" +
      "</div>" +
      "<label style='margin:0;font-size:11px'>marks</label>" +
    "</div>";
  list.appendChild(card);
  updateQuestionChip();
}

function updateQuestionChip() {
  var n    = document.querySelectorAll(".question-card").length;
  var chip = document.getElementById("questionCountChip");
  if (chip) chip.textContent = n + " question" + (n !== 1 ? "s" : "");
  var num = document.getElementById("numQuestions");
  if (num) num.value = n;
}

function removeRow(btn, cls, chipId, type) {
  btn.closest("." + cls).remove();
  if (type === "student") updateStudentChip();
  else                    updateQuestionChip();
}

function updateChip(chipId, type) {
  if (type === "student") updateStudentChip();
  else                    updateQuestionChip();
}

// ── Build Review ──────────────────────────────────────────────────────────────
function buildReview() {
  var title    = document.getElementById("examTitle").value    || "—";
  var subject  = document.getElementById("examSubject").value  || "—";
  var marks    = document.getElementById("totalMarks").value   || "—";
  var duration = document.getElementById("duration").value     || "—";
  var dlDate   = document.getElementById("deadlineDate").value || "—";
  var dlTime   = document.getElementById("deadlineTime").value || "—";
  var college  = getCollegeName() || "—";

  document.getElementById("reviewGrid").innerHTML =
    "<div class='review-item'><div class='r-label'>Title</div><div class='r-val'>" + title + "</div></div>" +
    "<div class='review-item'><div class='r-label'>Subject</div><div class='r-val'>" + subject + "</div></div>" +
    "<div class='review-item'><div class='r-label'>Total Marks</div><div class='r-val'>" + marks + "</div></div>" +
    "<div class='review-item'><div class='r-label'>Duration</div><div class='r-val'>" + duration + " min</div></div>" +
    "<div class='review-item'><div class='r-label'>Deadline</div><div class='r-val'>" + dlDate + " " + dlTime + "</div></div>" +
    "<div class='review-item'><div class='r-label'>College</div><div class='r-val' style='color:var(--primary);font-weight:700;'>🏫 " + college + "</div></div>";

  // Questions
  var cards = document.querySelectorAll(".question-card");
  document.getElementById("reviewQCount").textContent = cards.length;
  var qHTML = "";
  cards.forEach(function (card, i) {
    var qText   = card.querySelector(".q-text").value           || "(empty)";
    var correct = card.querySelector(".correct-select").value   || "?";
    var qMarks  = card.querySelector(".q-marks").value          || "1";
    qHTML +=
      "<li><strong>Q" + (i + 1) + ".</strong> " + qText +
      " &nbsp;<span class='ans'>✓ " + correct + "</span>" +
      " &nbsp;<span style='color:var(--muted-foreground);font-size:11px'>" +
      "[" + qMarks + " mark" + (qMarks > 1 ? "s" : "") + "]</span></li>";
  });
  document.getElementById("reviewQuestions").innerHTML =
    qHTML || "<li style='color:var(--muted-foreground)'>No questions added.</li>";

  // Students
  var allSelected = getSelectedStudents();
  document.getElementById("reviewSCount").textContent = allSelected.length;
  var sHTML = "";
  selectedStudentIds.forEach(function (id) {
    var s = allRegisteredStudents.find(function (x) { return x.id === id; });
    if (s) {
      sHTML += "<li>✅ <strong>" + s.fullName + "</strong>" +
        " <span style='color:var(--muted-foreground);font-size:11px;'>" +
        "— " + s.rollNo + " · " + s.email + "</span></li>";
    }
  });
  document.querySelectorAll(".student-email-input").forEach(function (input) {
    var email = input.value.trim();
    if (email) sHTML += "<li>📧 <strong>" + email + "</strong></li>";
  });
  document.getElementById("reviewStudents").innerHTML =
    sHTML || "<li style='color:var(--muted-foreground)'>No students added.</li>";

  goToStep(4);
}

// ── Publish Exam ──────────────────────────────────────────────────────────────
async function publishExam() {
  var title    = document.getElementById("examTitle").value.trim();
  var duration = parseInt(document.getElementById("duration").value)     || 0;
  var marks    = parseInt(document.getElementById("totalMarks").value)   || 0;
  var dlDate   = document.getElementById("deadlineDate").value;
  var dlTime   = document.getElementById("deadlineTime").value;
  var deadline = (dlDate && dlTime) ? dlDate + " " + dlTime + ":00"
               : dlDate ? dlDate + " 23:59:59" : null;

  // ── Collect questions ──────────────────────────────────────────────────────
  var cards    = document.querySelectorAll(".question-card");
  if (cards.length === 0) {
    showToast("⚠️ Add at least one question.", "error"); return;
  }
  var questions = [];
  var hasError  = false;
  cards.forEach(function (card, i) {
    if (hasError) return;
    var qText   = card.querySelector(".q-text").value.trim();
    var optA    = card.querySelector(".opt-a").value.trim();
    var optB    = card.querySelector(".opt-b").value.trim();
    var optC    = card.querySelector(".opt-c").value.trim();
    var optD    = card.querySelector(".opt-d").value.trim();
    var correct = card.querySelector(".correct-select").value;
    var qMarks  = parseInt(card.querySelector(".q-marks").value) || 1;
    if (!qText || !optA || !optB || !optC || !optD || !correct) {
      showToast("⚠️ Question " + (i + 1) + " is incomplete.", "error");
      hasError = true; return;
    }
    questions.push({
      questionText: qText, optionA: optA, optionB: optB,
      optionC: optC,       optionD: optD, correctOption: correct, marks: qMarks
    });
  });
  if (hasError) return;

  // ── Collect students ───────────────────────────────────────────────────────
  var students = getSelectedStudents();

  var sendEmail = document.getElementById("sendEmailToggle")
                  ? document.getElementById("sendEmailToggle").checked : false;

  console.log("❓ Questions:", questions.length, "| Students:", students.length,
              "| College:", getCollegeName());

  // ── STEP 1: Create exam ────────────────────────────────────────────────────
  showOverlay("Step 1 of 3 — Creating exam…");
  var examId;
  try {
    var params = new URLSearchParams({
      title: title, durationMinutes: duration, totalMarks: marks
    });
    var r1   = await fetch(BASE + "/add-exam", {
      method: "POST", credentials: "include",
      headers: formHeaders(), body: params.toString()
    });
    var d1 = await r1.json();
    console.log("✅ add-exam:", d1);
    if (!d1.success) { hideOverlay(); showToast("❌ " + d1.message, "error"); return; }
    examId = d1.examId;
    createdExamId = examId;
  } catch (e) {
    hideOverlay(); showToast("❌ Cannot reach server.", "error"); return;
  }

  // ── STEP 2: Save questions ─────────────────────────────────────────────────
  setOverlayMsg("Step 2 of 3 — Saving " + questions.length + " question(s)…");
  for (var i = 0; i < questions.length; i++) {
    try {
      var q      = questions[i];
      var qPrms  = new URLSearchParams({
        examId:        examId,
        questionText:  q.questionText,
        optionA:       q.optionA,
        optionB:       q.optionB,
        optionC:       q.optionC,
        optionD:       q.optionD,
        correctOption: q.correctOption,
        marks:         q.marks
      });
      var rq = await fetch(BASE + "/add-question", {
        method: "POST", credentials: "include",
        headers: formHeaders(), body: qPrms.toString()
      });
      var dq = await rq.json();
      if (!dq.success) {
        hideOverlay();
        showToast("❌ Failed on question " + (i + 1) + ": " + dq.message, "error");
        return;
      }
    } catch (e) {
      hideOverlay(); showToast("❌ Error saving question " + (i + 1), "error"); return;
    }
  }

  // ── STEP 3: Publish + enroll ───────────────────────────────────────────────
  setOverlayMsg("Step 3 of 3 — Publishing to " + students.length + " student(s)…");
  try {
    var payload = {
      examId:      examId,
      deadline:    deadline,
      students:    students,
      sendEmail:   sendEmail,
      collegeName: getCollegeName(),
      batchId:     batchId
    };
    var r3 = await fetch(BASE + "/publish-exam", {
      method: "POST", credentials: "include",
      headers: jsonHeaders(), body: JSON.stringify(payload)
    });
    var d3 = await r3.json();
    console.log("✅ publish-exam:", d3);
    hideOverlay();
    if (d3.success) {
      var msg =
        (d3.enrolled  > 0 ? d3.enrolled  + " student(s) enrolled. " : "") +
        (d3.emailSent > 0 ? "📧 " + d3.emailSent + " email(s) sent. " : "") +
        (d3.notFound  > 0 ? "⚠️ " + d3.notFound  + " roll number(s) not found." : "");
      document.getElementById("successMsg").textContent =
        msg || "Exam published successfully!";
      goToStep(5);
    } else {
      showToast("❌ " + d3.message, "error");
    }
  } catch (e) {
    hideOverlay(); showToast("❌ Cannot reach server.", "error");
  }
}

// ── Reset ─────────────────────────────────────────────────────────────────────
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
  questionCount     = 0;
  studentCount      = 0;
  createdExamId     = null;
  allRegisteredStudents = [];
  allGroupedStudents    = {};
  selectedStudentIds.clear();
  var bSelect = document.getElementById('batchSelect');
  if (bSelect) { bSelect.innerHTML = '<option value="">— No batch, add students manually below —</option>'; }
  var bInfo = document.getElementById('batchInfo');
  if (bInfo) bInfo.style.display = 'none';
  var bCount = document.getElementById('batchStudentCount');
  if (bCount) bCount.textContent = '';
  var regList = document.getElementById("registeredList");
  if (regList) regList.innerHTML = "";
  var loadLabel = document.getElementById("loadingLabel");
  if (loadLabel) loadLabel.textContent = "Loading students…";
  updateStudentChip();
  updateQuestionChip();
  goToStep(1);
}

// ── Logout ─────────────────────────────────────────────────────────────────────
function handleLogout() {
  sessionStorage.clear();
  fetch(BASE + "/logout", { credentials: "include" }).catch(function () {});
  window.location.href = "/";
}

// ── Overlay helpers ────────────────────────────────────────────────────────────
function showOverlay(msg) {
  var el = document.getElementById("publishingOverlay");
  if (el) { el.classList.add("show"); }
  setOverlayMsg(msg);
}
function hideOverlay() {
  var el = document.getElementById("publishingOverlay");
  if (el) el.classList.remove("show");
}
function setOverlayMsg(msg) {
  var el = document.getElementById("publishingMsg");
  if (el) el.textContent = msg;
}

// ── Toast ──────────────────────────────────────────────────────────────────────
function showToast(msg, type) {
  var t = document.getElementById("toast");
  if (!t) return;
  t.textContent  = msg;
  t.className    = "show " + (type || "info");
  clearTimeout(t._timer);
  t._timer = setTimeout(function () { t.className = ""; }, 4000);
}