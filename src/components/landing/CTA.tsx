import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import { useState } from "react";
import AuthModal from "@/components/landing/AuthModal";

const CTA = () => {
  const [modalOpen, setModalOpen] = useState(false);
  return (
    <>
    <section className="py-24 bg-background">
      <div className="max-w-4xl mx-auto px-6 text-center">
        <div className="hero-gradient rounded-2xl p-12 sm:p-16 relative overflow-hidden">
          <div className="absolute inset-0 opacity-10">
            <div className="absolute top-0 right-0 w-64 h-64 rounded-full bg-accent blur-3xl" />
          </div>
          <div className="relative">
            <h2 className="text-3xl sm:text-4xl font-bold text-primary-foreground mb-4">
              Ready to modernize your exams?
            </h2>
            <p className="text-primary-foreground/70 text-lg mb-8 max-w-xl mx-auto">
              Join hundreds of universities and companies already using ExamFlow to deliver secure, scalable online assessments.
            </p>
            <Button variant="hero" size="lg" onClick={() => setModalOpen(true)}>
              Get started for free
              <ArrowRight className="ml-1 h-5 w-5" />
            </Button>
            <p className="text-primary-foreground/40 text-sm mt-4">No credit card required · Free for up to 50 students</p>
          </div>
        </div>
      </div>
    </section>
     {/* Auth Modal */}
      <AuthModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </>
  );
};

export default CTA;
