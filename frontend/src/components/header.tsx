"use client";

import Link from "next/link";
import { useQueryClient } from "@tanstack/react-query";
import { signout } from "@/lib/api";
import { useAuth } from "@/lib/auth";

export const Header = ({ className }: { className?: string }) => {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  return (
    <header className={className}>
      <div className="flex items-center max-w-2xl mx-auto">
        <div>Scala Web App</div>
        <ul className="flex ml-auto">
          {user ? (
            <>
              <li>
                <Link href="/dashboard" className="px-4 py-2 inline-block">
                  Dashboard
                </Link>
              </li>
              <li>
                <Link href="/profile" className="px-4 py-2 inline-block">
                  Profile
                </Link>
              </li>
              <li>
                <a
                  role="button"
                  onClick={async () => {
                    await signout();
                    window.localStorage.removeItem("userId");
                    queryClient.resetQueries({ queryKey: ["user"] });
                  }}
                  className="px-4 py-2 inline-block"
                >
                  Sign out
                </a>
              </li>
            </>
          ) : (
            <>
              <li>
                <Link href="/signup" className="px-4 py-2 inline-block">
                  Sign up
                </Link>
              </li>
              <li>
                <Link href="/signin" className="px-4 py-2 inline-block">
                  Sign in
                </Link>
              </li>
            </>
          )}
        </ul>
      </div>
    </header>
  );
};
