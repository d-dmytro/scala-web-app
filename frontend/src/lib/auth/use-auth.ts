import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { getCurrentUser } from "../api";
import { User } from "../api/types";

export const useAuth = ({ redirect }: { redirect?: boolean } = {}) => {
  const router = useRouter();
  const {
    data: user,
    isSuccess,
    isRefetching,
  } = useQuery<User>({
    queryKey: ["user"],
    queryFn: getCurrentUser,
    // refetchOnMount: "always",
    staleTime: 0,
  });

  useEffect(() => {
    if (!isRefetching && isSuccess && !user && redirect) {
      router.push("/signin");
    }
  }, [isRefetching, isSuccess, user, redirect]);

  return { user };
};
