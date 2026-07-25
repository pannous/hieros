import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Neolithic Sites / Ancient Architects Map",
  description:
    "An interactive Fertile Crescent Neolithic map linking archaeological sites to Ancient Architects videos.",
  icons: {
    icon: "/favicon.svg",
    shortcut: "/favicon.svg",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
