"use client";

import { signin } from "@/lib/api";
import { User } from "@/lib/api/types";
import { DefaultError, useMutation } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { SubmitHandler, useForm } from "react-hook-form";
import { Button } from "./button";

interface FieldValues {
  email: string;
  password: string;
}

export default function SigninForm() {
  const router = useRouter();
  const { data: user, mutate } = useMutation<User, DefaultError, FieldValues>({
    mutationKey: ["user"],
    mutationFn: signin,
  });

  const { register, handleSubmit } = useForm<FieldValues>();

  const onSubmit: SubmitHandler<FieldValues> = (values) => {
    mutate(values);
  };

  useEffect(() => {
    if (user) {
      window.localStorage.setItem("userId", user.id);
      router.push("/dashboard");
    }
  }, [user]);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className="mb-4">
        <label htmlFor="email">Email</label>
        <div>
          <input
            type="email"
            id="email"
            className="border p-2 w-full"
            {...register("email", { required: true })}
          />
        </div>
      </div>
      <div className="mb-4">
        <label htmlFor="password">Password</label>
        <div>
          <input
            type="password"
            id="password"
            className="border p-2 w-full"
            {...register("password", { required: true })}
          />
        </div>
      </div>
      <div>
        <Button type="submit">Sign in</Button>
      </div>
    </form>
  );
}
