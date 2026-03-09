import { Button } from "@/components/ui/button";
import { GraduationCap, Menu, X } from "lucide-react";
import { useState } from "react";
import AuthModal from "@/components/landing/AuthModal";

const Navbar = () => {
  const [menuOpen,  setMenuOpen]  = useState(false);
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
    <nav className="hero-gradient sticky top-0 z-50 border-b border-primary-foreground/10">
      <div className="max-w-7xl mx-auto px-6 flex items-center justify-between h-16">
        <div className="flex items-center gap-2 text-primary-foreground font-display font-bold text-xl">
          <GraduationCap className="h-7 w-7 text-accent" />
          ExamFlow
        </div>

        {/* Desktop */}
        <div className="hidden md:flex items-center gap-8">
          <a href="#features" className="text-sm text-primary-foreground/70 hover:text-primary-foreground transition-colors">Features</a>
          <a href="#how-it-works" className="text-sm text-primary-foreground/70 hover:text-primary-foreground transition-colors">How It Works</a>
          <Button variant="hero" size="sm" onClick={() => setModalOpen(true)}>Get Started</Button>
        </div>

        {/* Mobile toggle */}
        <button className="md:hidden text-primary-foreground" onClick={() => setMenuOpen(!menuOpen)}>
          {menuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {/* Mobile menu */}
      {menuOpen && (
        <div className="md:hidden px-6 pb-4 flex flex-col gap-3">
          <a href="#features" className="text-sm text-primary-foreground/70 py-2">Features</a>
          <a href="#how-it-works" className="text-sm text-primary-foreground/70 py-2">How It Works</a>
          <Button variant="hero" size="sm" className="w-full" onClick={() => setModalOpen(true)}>Get Started</Button>
        </div>
      )}
    </nav>
    {/* Auth Modal */}
      <AuthModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </>
  );
};

export default Navbar;
