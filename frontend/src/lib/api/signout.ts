import { client } from "./client";

export const signout = async (): Promise<void> => {
  await client.post("/signout", undefined, { withCredentials: true });
};
