"use client";

import { useAuth } from "@/lib/auth";

export default function ProfilePage() {
  const { user } = useAuth({ redirect: true });

  return user ? (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Profile</h1>
    </div>
  ) : null;
}
