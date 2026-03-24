import { useState, useEffect, useRef } from "react";
import {
  X, GraduationCap, Eye, EyeOff,
  ShieldCheck, Users, Building2,
} from "lucide-react";
import { cn } from "@/lib/utils";

// ─── Types ────────────────────────────────────────────────────────────────────
type Tab  = "student" | "admin" | "college";
type Mode = "login"   | "signup";

type ClassLevel =
  | "" | "1" | "2" | "3" | "4" | "5"
  | "6" | "7" | "8" | "9" | "10"
  | "11" | "12" | "UG" | "PG";

type Stream = "" | "Science" | "Arts" | "Commerce";

interface AuthModalProps {
  open: boolean;
  onClose: () => void;
}

// ─── Base input class ─────────────────────────────────────────────────────────
const inputCls = cn(
  "w-full rounded-xl border border-border bg-background px-4 py-2.5 text-sm text-foreground",
  "placeholder:text-muted-foreground/60 outline-none transition-all duration-200",
  "focus:border-[hsl(243,75%,58%)] focus:ring-2 focus:ring-[hsl(243,75%,58%)]/20",
);

// ─── Field component ──────────────────────────────────────────────────────────
function Field({
  label, type = "text", placeholder, value, onChange,
}: {
  label: string; type?: string;
  placeholder?: string; value: string;
  onChange: (v: string) => void;
}) {
  const [show, setShow] = useState(false);
  const isPassword = type === "password";
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </label>
      <div className="relative">
        <input
          type={isPassword && show ? "text" : type}
          placeholder={placeholder}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={cn(inputCls, isPassword && "pr-11")}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setShow(!show)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
          >
            {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
          </button>
        )}
      </div>
    </div>
  );
}

// ─── Select component ─────────────────────────────────────────────────────────
function SelectField({
  label, value, onChange, options, placeholder,
}: {
  label: string; value: string;
  onChange: (v: string) => void;
  options: { value: string; label: string }[];
  placeholder?: string;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
        {label}
      </label>
      <div className="relative">
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={cn(inputCls, "appearance-none pr-10 cursor-pointer")}
        >
          <option value="">{placeholder ?? "Select…"}</option>
          {options.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
        <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M2 4l4 4 4-4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </div>
      </div>
    </div>
  );
}

// ─── Constants ────────────────────────────────────────────────────────────────
const CLASS_OPTIONS: { value: ClassLevel; label: string }[] = [
  { value: "1",  label: "Class 1"  }, { value: "2",  label: "Class 2"  },
  { value: "3",  label: "Class 3"  }, { value: "4",  label: "Class 4"  },
  { value: "5",  label: "Class 5"  }, { value: "6",  label: "Class 6"  },
  { value: "7",  label: "Class 7"  }, { value: "8",  label: "Class 8"  },
  { value: "9",  label: "Class 9"  }, { value: "10", label: "Class 10" },
  { value: "11", label: "Class 11" }, { value: "12", label: "Class 12" },
  { value: "UG", label: "Under Graduate (UG)" },
  { value: "PG", label: "Post Graduate (PG)"  },
];

const STREAM_OPTIONS = [
  { value: "Science",  emoji: "🔬", label: "Science"  },
  { value: "Arts",     emoji: "🎨", label: "Arts"     },
  { value: "Commerce", emoji: "💼", label: "Commerce" },
];

const needsStream = (c: ClassLevel) => ["11","12","UG","PG"].includes(c);
const needsCourse = (c: ClassLevel) => c === "UG";

const BASE = "http://localhost:8085/Online_Examination_System";

// ─── Tab config ───────────────────────────────────────────────────────────────
const TABS: { id: Tab; label: string; icon: React.ReactNode; desc: string }[] = [
  {
    id:   "student",
    label: "Student",
    icon:  <Users className="h-4 w-4" />,
    desc:  "Access and attempt exams assigned to you",
  },
  {
    id:   "admin",
    label: "Admin",
    icon:  <ShieldCheck className="h-4 w-4" />,
    desc:  "Manage exams, students, and results",
  },
  {
    id:   "college",
    label: "College",
    icon:  <Building2 className="h-4 w-4" />,
    desc:  "Register your institution on ExamFlow",
  },
];

// ─── Main component ───────────────────────────────────────────────────────────
export default function AuthModal({ open, onClose }: AuthModalProps) {

  const [tab,  setTab]  = useState<Tab>("student");
  const [mode, setMode] = useState<Mode>("login");
  const overlayRef      = useRef<HTMLDivElement>(null);

  const [submitState, setSubmitState] = useState<{
    loading: boolean; error: string; success: string;
  }>({ loading: false, error: "", success: "" });

  // Common fields
  const [name,     setName]     = useState("");
  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [confirm,  setConfirm]  = useState("");

  // Admin fields
  const [adminKey,     setAdminKey]     = useState("");
  const [adminCollege, setAdminCollege] = useState("");

  // Student signup fields
  const [rollNo,     setRollNo]     = useState("");
  const [college,    setCollege]    = useState("");
  const [classLevel, setClassLevel] = useState<ClassLevel>("");
  const [stream,     setStream]     = useState<Stream>("");
  const [course,     setCourse]     = useState("");
  const [accessKey,  setAccessKey]  = useState("");

  // College registration fields
  const [collegeName,      setCollegeName]      = useState("");
  const [collegeEmail,     setCollegeEmail]     = useState("");
  const [collegePassword,  setCollegePassword]  = useState("");
  const [collegeConfirm,   setCollegeConfirm]   = useState("");
  const [collegeAccessKey, setCollegeAccessKey] = useState("");
  const [collegeAdminName, setCollegeAdminName] = useState("");

  // Reset on tab / mode change
  useEffect(() => {
    setName(""); setEmail(""); setPassword(""); setConfirm("");
    setAdminKey(""); setAdminCollege("");
    setRollNo(""); setCollege(""); setClassLevel(""); setStream(""); setCourse(""); setAccessKey("");
    setCollegeName(""); setCollegeEmail(""); setCollegePassword("");
    setCollegeConfirm(""); setCollegeAccessKey(""); setCollegeAdminName("");
    setSubmitState({ loading: false, error: "", success: "" });
  }, [tab, mode]);

  useEffect(() => { setStream(""); setCourse(""); }, [classLevel]);

  useEffect(() => {
    const h = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    window.addEventListener("keydown", h);
    return () => window.removeEventListener("keydown", h);
  }, [onClose]);

  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  if (!open) return null;

  const isSignup        = mode === "signup";
  const isStudentSignup = tab === "student" && isSignup;
  const isAdminSignup   = tab === "admin"   && isSignup;
  const showStream      = isStudentSignup && needsStream(classLevel);
  const showCourse      = isStudentSignup && needsCourse(classLevel);

  // ── Submit ─────────────────────────────────────────────────────────────────
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitState({ loading: true, error: "", success: "" });

    // ── COLLEGE REGISTRATION ────────────────────────────────────────────────
    if (tab === "college") {
      if (!collegeName.trim()) {
        setSubmitState({ loading: false, error: "College name is required.", success: "" });
        return;
      }
      if (!collegeAccessKey.trim()) {
        setSubmitState({ loading: false, error: "Please create an access key.", success: "" });
        return;
      }
      if (collegePassword !== collegeConfirm) {
        setSubmitState({ loading: false, error: "Passwords do not match.", success: "" });
        return;
      }

      const payload = {
        collegeName:    collegeName,
        adminName:      collegeAdminName || "Admin",
        email:          collegeEmail,
        password:       collegePassword,
        confirmPassword:collegeConfirm,
        accessKey:      collegeAccessKey,
      };

      try {
        const res  = await fetch(`${BASE}/api/college/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body:    JSON.stringify(payload),
        });
        const data = await res.json();
        if (data.success) {
          setSubmitState({
            loading: false, error: "",
            success: "✅ College registered! You can now sign in as Admin.",
          });
        } else {
          setSubmitState({ loading: false, error: data.message, success: "" });
        }
      } catch {
        setSubmitState({
          loading: false,
          error:   "Could not reach the server. Make sure Tomcat is running.",
          success: "",
        });
      }
      return;
    }

    // ── STUDENT / ADMIN SIGNUP ──────────────────────────────────────────────
    if (isSignup) {
      const payload =
        tab === "student"
          ? { role:"student", fullName:name, rollNo, email, password,
              confirmPassword:confirm, collegeName:college, accessKey, classLevel, stream, course }
          : { role:"admin", fullName:name, email, password,
              confirmPassword:confirm, collegeName:adminCollege, adminAccessKey:adminKey };

      try {
        const res  = await fetch(`${BASE}/api/register`, {
          method:  "POST",
          headers: { "Content-Type": "application/json" },
          body:    JSON.stringify(payload),
        });
        const data = await res.json();
        if (data.success) {
          setSubmitState({ loading: false, error: "", success: data.message });
        } else {
          setSubmitState({ loading: false, error: data.message, success: "" });
        }
      } catch {
        setSubmitState({
          loading: false,
          error:   "Could not reach the server. Make sure Tomcat is running.",
          success: "",
        });
      }
      return;
    }

    // ── LOGIN ───────────────────────────────────────────────────────────────
    const payload =
      tab === "student"
        ? { role:"student", identifier:email, password }
        : { role:"admin",   identifier:email, password, adminKey };

    try {
      const res  = await fetch(`${BASE}/api/login`, {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify(payload),
      });
      const data = await res.json();

      if (data.success) {
        setSubmitState({ loading: false, error: "", success: data.message });

        if (data.role === "admin") {
          sessionStorage.setItem("adminName",   data.adminName   || "Admin");
          sessionStorage.setItem("adminId",     String(data.adminId  || ""));
          sessionStorage.setItem("collegeName", data.collegeName || "");
        } else if (data.role === "student") {
          sessionStorage.setItem("studentId",   String(data.studentId   || ""));
          sessionStorage.setItem("studentName", data.studentName || "Student");
          sessionStorage.setItem("rollNo",      data.rollNo      || "");
          sessionStorage.setItem("collegeName", data.collegeName || "");
        }

        setTimeout(() => {
          if (data.role === "admin") {
            window.location.href = window.location.origin + "/admin-panel.html";
          } else {
            window.location.href = window.location.origin + "/student-dashboard.html";
          }
        }, 1000);
      } else {
        setSubmitState({ loading: false, error: data.message, success: "" });
      }
    } catch {
      setSubmitState({
        loading: false,
        error:   "Could not reach the server. Make sure Tomcat is running.",
        success: "",
      });
    }
  };

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div
      ref={overlayRef}
      onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ background: "hsl(220 65% 8% / 0.72)", backdropFilter: "blur(6px)" }}
    >
      <div
        className={cn(
          "relative w-full max-w-md rounded-2xl border border-border bg-card shadow-2xl",
          "animate-in fade-in-0 zoom-in-95 duration-200",
          "max-h-[92vh] overflow-y-auto",
        )}
        style={{ boxShadow: "0 24px 64px -12px hsl(220 65% 8% / 0.35)" }}
      >
        {/* Gradient top bar */}
        <div
          className="sticky top-0 h-1 rounded-t-2xl z-10"
          style={{ background: "linear-gradient(90deg, hsl(243,75%,58%), hsl(258,89%,66%))" }}
        />

        {/* Header */}
        <div className="px-7 pt-7 pb-4">
          <div className="flex items-center justify-between mb-5">
            <div className="flex items-center gap-2">
              <GraduationCap className="h-6 w-6" style={{ color: "hsl(258,89%,66%)" }} />
              <span className="font-display font-bold text-xl text-foreground tracking-tight">
                ExamFlow
              </span>
            </div>
            <button
              onClick={onClose}
              className="rounded-lg p-1.5 text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
          <h2 className="font-display font-bold text-2xl text-foreground tracking-tight mb-1">
            {tab === "college"
              ? "Register your College"
              : isSignup ? "Create your account" : "Welcome back"}
          </h2>
          <p className="text-sm text-muted-foreground">
            {tab === "college"
              ? "Set up your institution on ExamFlow"
              : isSignup
              ? "Join ExamFlow and start your journey"
              : "Sign in to continue to your dashboard"}
          </p>
        </div>

        {/* ── Three-tab switcher ───────────────────────────────────────────── */}
        <div className="px-7 mb-5">
          <div className="flex rounded-xl border border-border bg-muted/40 p-1 gap-1">
            {TABS.map((t) => (
              <button
                key={t.id}
                type="button"
                onClick={() => setTab(t.id)}
                className={cn(
                  "flex-1 flex items-center justify-center gap-1.5 rounded-lg py-2 text-xs font-semibold transition-all duration-200",
                  tab === t.id
                    ? "bg-card text-foreground shadow-sm border border-border"
                    : "text-muted-foreground hover:text-foreground",
                )}
              >
                {t.icon}
                {t.label}
              </button>
            ))}
          </div>
          <p className="mt-2 text-xs text-center text-muted-foreground">
            {TABS.find((t) => t.id === tab)?.desc}
          </p>
        </div>

        {/* ══════════════════════════════════════════════════════════════════
            COLLEGE REGISTRATION FORM
        ══════════════════════════════════════════════════════════════════ */}
        {tab === "college" && (
          <form onSubmit={handleSubmit} className="px-7 pb-4 flex flex-col gap-4">

            {/* Info banner */}
            <div
              className="flex items-start gap-2.5 rounded-xl px-3.5 py-3 text-xs"
              style={{
                background: "hsl(243,75%,58%,0.07)",
                color:      "hsl(243,75%,38%)",
                border:     "1px solid hsl(243,75%,58%,0.2)",
              }}
            >
              <Building2 className="h-4 w-4 flex-shrink-0 mt-0.5" />
              <span>
                Register your school or college to get an <strong>Access Key</strong>.
                Share this key with your admin staff so they can sign in and manage exams.
              </span>
            </div>

            <Field
              label="College / School Name"
              placeholder="e.g. St. Xavier's College, Mumbai"
              value={collegeName}
              onChange={setCollegeName}
            />

            <Field
              label="Admin / Principal Name"
              placeholder="e.g. Dr. Ramesh Kumar"
              value={collegeAdminName}
              onChange={setCollegeAdminName}
            />

            <Field
              label="Admin Email Address"
              type="email"
              placeholder="admin@yourcollege.edu"
              value={collegeEmail}
              onChange={setCollegeEmail}
            />

            {/* Access key section */}
            <div
              className="flex items-start gap-2.5 rounded-xl px-3.5 py-3 text-xs"
              style={{
                background: "hsl(38,92%,50%,0.08)",
                color:      "hsl(32,95%,34%)",
                border:     "1px solid hsl(38,92%,50%,0.25)",
              }}
            >
              <ShieldCheck className="h-4 w-4 flex-shrink-0 mt-0.5" />
              <span>
                Create a secret <strong>College Access Key</strong>.
                Only admins of your institution should know this key.
                Keep it safe — it cannot be recovered.
              </span>
            </div>

            <Field
              label="Create College Access Key"
              type="password"
              placeholder="e.g. XYZSchool@2026 (min 8 chars)"
              value={collegeAccessKey}
              onChange={setCollegeAccessKey}
            />

            <Field
              label="Admin Password"
              type="password"
              placeholder="Your personal login password"
              value={collegePassword}
              onChange={setCollegePassword}
            />

            <Field
              label="Confirm Password"
              type="password"
              placeholder="Re-enter password"
              value={collegeConfirm}
              onChange={setCollegeConfirm}
            />

            {/* Error */}
            {submitState.error && (
              <div
                className="flex items-center gap-2 rounded-xl px-3.5 py-3 text-xs font-medium"
                style={{
                  background: "hsl(0,72%,50%,0.08)",
                  color:      "hsl(0,72%,40%)",
                  border:     "1px solid hsl(0,72%,50%,0.2)",
                }}
              >
                <span className="text-base">⚠️</span>
                {submitState.error}
              </div>
            )}

            {/* Success */}
            {submitState.success && (
              <div
                className="flex items-center gap-2 rounded-xl px-3.5 py-3 text-xs font-medium"
                style={{
                  background: "hsl(142,76%,36%,0.08)",
                  color:      "hsl(142,76%,28%)",
                  border:     "1px solid hsl(142,76%,36%,0.2)",
                }}
              >
                <span className="text-base">✅</span>
                {submitState.success}
              </div>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={submitState.loading}
              className={cn(
                "w-full flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-bold text-white transition-all duration-200",
                submitState.loading ? "opacity-60 cursor-not-allowed" : "hover:opacity-90",
              )}
              style={{ background: "linear-gradient(135deg, hsl(243,75%,58%), hsl(258,89%,66%))" }}
            >
              {submitState.loading
                ? <><span className="animate-spin">⏳</span> Registering…</>
                : <>🏫 Register College</>}
            </button>

            <p className="text-xs text-center text-muted-foreground">
              Already registered?{" "}
              <button
                type="button"
                onClick={() => setTab("admin")}
                className="font-semibold underline"
                style={{ color: "hsl(243,75%,58%)" }}
              >
                Sign in as Admin →
              </button>
            </p>
          </form>
        )}

        {/* ══════════════════════════════════════════════════════════════════
            STUDENT / ADMIN FORM
        ══════════════════════════════════════════════════════════════════ */}
        {tab !== "college" && (
          <form onSubmit={handleSubmit} className="px-7 pb-4 flex flex-col gap-4">

            {/* Full name — signup */}
            {isSignup && (
              <Field label="Full Name" placeholder="e.g. Arjun Sharma"
                value={name} onChange={setName} />
            )}

            {/* Roll number — student signup */}
            {isStudentSignup && (
              <Field label="Roll Number" placeholder="e.g. 2024CS001"
                value={rollNo} onChange={setRollNo} />
            )}

            {/* Email / identifier */}
            <Field
              label={tab === "student" && !isSignup ? "Roll Number / Email" : "Email Address"}
              placeholder={
                tab === "student" && !isSignup
                  ? "e.g. 2024CS001 or you@college.edu"
                  : "you@institution.edu"
              }
              value={email}
              onChange={setEmail}
            />

            {/* College name — student signup */}
            {isStudentSignup && (
              <Field
                label="College / School Name"
                placeholder="e.g. St. Xavier's College, Mumbai"
                value={college}
                onChange={setCollege}
              />
            )}

            {/* Admin signup — college + access key */}
            {isAdminSignup && (
              <>
                <Field
                  label="College / Institution Name"
                  placeholder="e.g. St. Xavier's College, Mumbai"
                  value={adminCollege}
                  onChange={setAdminCollege}
                />
                <div
                  className="flex items-start gap-2.5 rounded-xl px-3.5 py-3 text-xs"
                  style={{
                    background: "hsl(38,92%,50%,0.08)",
                    color:      "hsl(32,95%,34%)",
                    border:     "1px solid hsl(38,92%,50%,0.25)",
                  }}
                >
                  <ShieldCheck className="h-4 w-4 flex-shrink-0 mt-0.5" />
                  <span>
                    The <strong>Admin Access Key</strong> was set when your college
                    was registered. Contact your institution if you don't have it.
                  </span>
                </div>
                <Field
                  label="Admin Access Key"
                  type="password"
                  placeholder="Enter the key issued by your institution"
                  value={adminKey}
                  onChange={setAdminKey}
                />
              </>
            )}

            {/* Access key — student signup (links to college) */}
            {isStudentSignup && (
              <>
                <div
                  className="flex items-start gap-2.5 rounded-xl px-3.5 py-3 text-xs"
                  style={{
                    background: "hsl(243,75%,58%,0.07)",
                    color:      "hsl(243,75%,38%)",
                    border:     "1px solid hsl(243,75%,58%,0.2)",
                  }}
                >
                  <ShieldCheck className="h-4 w-4 flex-shrink-0 mt-0.5" />
                  <span>
                    Enter your <strong>College Access Key</strong> — issued by your
                    institution when they registered on ExamFlow.
                    This links your account to your college.
                  </span>
                </div>
                <Field
                  label="College Access Key"
                  type="password"
                  placeholder="Enter the key given by your institution"
                  value={accessKey}
                  onChange={setAccessKey}
                />
              </>
            )}

            {/* Class — student signup */}
            {isStudentSignup && (
              <SelectField
                label="Class / Year"
                value={classLevel}
                onChange={(v) => setClassLevel(v as ClassLevel)}
                options={CLASS_OPTIONS}
                placeholder="Select your class or year"
              />
            )}

            {/* Stream picker */}
            {showStream && (
              <div className="flex flex-col gap-2">
                <label className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Stream
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {STREAM_OPTIONS.map((s) => {
                    const active = stream === s.value;
                    return (
                      <button
                        key={s.value}
                        type="button"
                        onClick={() => setStream(s.value as Stream)}
                        className={cn(
                          "flex flex-col items-center justify-center gap-1.5 rounded-xl border py-3.5 px-2",
                          "text-xs font-semibold transition-all duration-200",
                          active
                            ? "border-[hsl(243,75%,58%)] bg-[hsl(243,75%,58%)]/10 text-[hsl(243,75%,58%)] shadow-sm"
                            : "border-border bg-background text-muted-foreground hover:border-[hsl(243,75%,58%)]/40 hover:text-foreground",
                        )}
                      >
                        <span className="text-xl">{s.emoji}</span>
                        <span>{s.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Course — UG only */}
            {showCourse && (
              <Field
                label="Course / Degree Programme"
                placeholder="e.g. B.Sc Computer Science · B.Com"
                value={course}
                onChange={setCourse}
              />
            )}

            {/* Password */}
            <Field label="Password" type="password"
              placeholder="Enter your password"
              value={password} onChange={setPassword} />

            {/* Confirm password — signup */}
            {isSignup && (
              <Field label="Confirm Password" type="password"
                placeholder="Re-enter your password"
                value={confirm} onChange={setConfirm} />
            )}

            {/* Admin access key — admin LOGIN only */}
            {tab === "admin" && !isSignup && (
              <>
                <div
                  className="flex items-start gap-2.5 rounded-xl px-3.5 py-2.5 text-xs"
                  style={{
                    background: "hsl(38,92%,50%,0.06)",
                    color:      "hsl(32,95%,34%)",
                    border:     "1px solid hsl(38,92%,50%,0.2)",
                  }}
                >
                  <ShieldCheck className="h-4 w-4 flex-shrink-0 mt-0.5" />
                  <span>Enter the access key set when your college was registered.</span>
                </div>
                <Field
                  label="Admin Access Key"
                  type="password"
                  placeholder="Your college access key"
                  value={adminKey}
                  onChange={setAdminKey}
                />
              </>
            )}

            {/* Forgot password */}
            {!isSignup && (
              <div className="flex justify-end -mt-1">
                <button
                  type="button"
                  className="text-xs font-medium transition-colors"
                  style={{ color: "hsl(243,75%,58%)" }}
                >
                  Forgot password?
                </button>
              </div>
            )}

            {/* Error */}
            {submitState.error && (
              <div
                className="flex items-center gap-2 rounded-xl px-3.5 py-3 text-xs font-medium"
                style={{
                  background: "hsl(0,72%,50%,0.08)",
                  color:      "hsl(0,72%,40%)",
                  border:     "1px solid hsl(0,72%,50%,0.2)",
                }}
              >
                <span className="text-base">⚠️</span>
                {submitState.error}
              </div>
            )}

            {/* Success */}
            {submitState.success && (
              <div
                className="flex items-center gap-2 rounded-xl px-3.5 py-3 text-xs font-medium"
                style={{
                  background: "hsl(142,76%,36%,0.08)",
                  color:      "hsl(142,76%,28%)",
                  border:     "1px solid hsl(142,76%,36%,0.2)",
                }}
              >
                <span className="text-base">✅</span>
                {submitState.success}
              </div>
            )}

            {/* Submit button */}
            <button
              type="submit"
              disabled={submitState.loading}
              className={cn(
                "w-full flex items-center justify-center gap-2 rounded-xl py-3 text-sm font-bold text-white transition-all duration-200",
                submitState.loading ? "opacity-60 cursor-not-allowed" : "hover:opacity-90",
              )}
              style={{ background: "linear-gradient(135deg, hsl(243,75%,58%), hsl(258,89%,66%))" }}
            >
              {submitState.loading
                ? <><span className="animate-spin">⏳</span> Please wait…</>
                : isSignup
                ? <>Create Account</>
                : <>Sign in as {tab.charAt(0).toUpperCase() + tab.slice(1)} →</>}
            </button>

            {/* Toggle login/signup */}
            <p className="text-xs text-center text-muted-foreground">
              {isSignup ? "Already have an account? " : "Don't have an account? "}
              <button
                type="button"
                onClick={() => setMode(isSignup ? "login" : "signup")}
                className="font-semibold underline"
                style={{ color: "hsl(243,75%,58%)" }}
              >
                {isSignup ? "Sign in →" : "Create account →"}
              </button>
                {tab === "admin" && !isSignup && (
                  <span className="block mt-1">
                    New institution?{" "}
                    <button
                      type="button"
                      onClick={() => setTab("college")}
                      className="font-semibold underline"
                      style={{ color: "hsl(243,75%,58%)" }}
                    >
                      Register your college →
                    </button>
                  </span>
                )}
              </p>
          </form>
        )}

        {/* Bottom padding */}
        <div className="h-4" />
      </div>
    </div>
  );
}