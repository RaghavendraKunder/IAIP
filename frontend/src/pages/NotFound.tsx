import { useLocation } from "react-router-dom";
import { useEffect } from "react";

const STATIC_PAGES = [
  "/admin-panel.html",
  "/student-portal.html",
  "/student-dashboard.html",
  "/student-exam.html",
  "/student-result.html",
];

const NotFound = () => {
  const location = useLocation();
  const isStatic = STATIC_PAGES.includes(location.pathname);

  useEffect(() => {
    if (!isStatic) {
      console.error("404:", location.pathname);
      return;
    }

    const guardKey = "nav_" + location.pathname;

    // If we already redirected once, stop — prevents infinite loop
    if (sessionStorage.getItem(guardKey) === "redirecting") {
      sessionStorage.removeItem(guardKey);
      return;
    }

    // Mark that we're redirecting, then do it
    sessionStorage.setItem(guardKey, "redirecting");
    window.location.href = location.pathname + location.search;

  }, []);

  if (isStatic) {
    return (
      <div style={{
        display: "flex", alignItems: "center",
        justifyContent: "center", minHeight: "100vh",
        fontFamily: "sans-serif", color: "#64748b", fontSize: "14px",
      }}>
        ⏳ Loading…
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted">
      <div className="text-center">
        <h1 className="mb-4 text-4xl font-bold">404</h1>
        <p className="mb-4 text-xl text-muted-foreground">Oops! Page not found</p>
        <a href="/" className="text-primary underline hover:text-primary/90">
          Return to Home
        </a>
      </div>
    </div>
  );
};

export default NotFound;