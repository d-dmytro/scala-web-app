import { client } from "./client";
import { User } from "./types";

interface SignupParams {
  email: string;
  password: string;
}

export const signup = async (params: SignupParams): Promise<User> => {
  const res = await client.post<User>("/signup", params, {
    withCredentials: true,
  });
  return res.data;
};
